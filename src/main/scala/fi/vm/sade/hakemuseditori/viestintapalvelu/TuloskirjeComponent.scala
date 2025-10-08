package fi.vm.sade.hakemuseditori.viestintapalvelu

import java.io.{File, FileInputStream, IOException}
import fi.vm.sade.hakemuseditori.auditlog.{Audit, FetchTuloskirje}
import fi.vm.sade.hakemuseditori.hakemus.domain.Tuloskirje
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.util.Logging

import javax.servlet.http.HttpServletRequest
import org.apache.commons.io.IOUtils
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.http.HttpStatusCode
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetBucketAclRequest, GetObjectRequest, GetObjectResponse, HeadObjectRequest, HeadObjectResponse, S3Exception, S3Object}
import software.amazon.awssdk.utils.Validate

import java.time.temporal.ChronoField
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait TuloskirjeComponent {
  val tuloskirjeService: TuloskirjeService

  private def getFileSuffix(tuloskirjeKind: TuloskirjeKind) = {
    tuloskirjeKind match {
      case Pdf => "pdf"
      case AccessibleHtml => "html"
    }
  }

  class StubbedTuloskirjeService extends TuloskirjeService with JsonFormats with Logging {
    override def fetchTuloskirje(request: HttpServletRequest,hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Array[Byte]] = {
      logger.info(s"Get tuloskirje info for hakemus $hakemusOid")
      hakemusOid match {
        case "1.2.246.562.11.00000441369" => Some("1.2.246.562.11.00000441369_hyvaksymiskirje".getBytes)
        case _ => None
      }
    }
    override def getTuloskirjeInfo(request: HttpServletRequest,hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Tuloskirje] ={
      fetchTuloskirje(request,hakuOid, hakemusOid, tuloskirjeKind).map(_ => Tuloskirje(hakuOid, 1479099404159L))
    }
  }

  class SharedDirTuloskirjeService(appConfig: AppConfig) extends TuloskirjeService with Logging {
    private val fileSystemUrl = appConfig.settings.tuloskirjeetFileSystemUrl

    override def fetchTuloskirje(request: HttpServletRequest,hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Array[Byte]] = {
      val file = getLocalFile(hakuOid, hakemusOid, tuloskirjeKind)
      if (file.exists()) {
        val fileStream = new FileInputStream(file)
        val byteArray: Array[Byte] = IOUtils.toByteArray(fileStream)
        IOUtils.closeQuietly(fileStream)
        Audit.oppija.log(FetchTuloskirje(request, hakuOid, hakemusOid))
        Some(byteArray)
      } else {
        logger.warn("Ei löytynyt tuloskirjettä: " + file)
        None
      }
    }

    override def getTuloskirjeInfo(request: HttpServletRequest, hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Tuloskirje] = {
      val file = getLocalFile(hakuOid, hakemusOid, tuloskirjeKind)
      if (file.exists()) {
        Some(Tuloskirje(hakuOid, file.lastModified()))
      } else {
        None
      }
    }
    private def getLocalFile(hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind): File = {
      val fileSuffix = getFileSuffix(tuloskirjeKind)
      new File(s"$fileSystemUrl/$hakuOid/$hakemusOid.$fileSuffix")
    }
  }

  class S3TulosKirjeService(appConfig: AppConfig) extends TuloskirjeService with JsonFormats with Logging {
    private val s3Settings = appConfig.settings.s3Settings
    private val s3client = S3Client.builder()
      .region(Region.of(s3Settings.region))
      .build()

    def doesBucketExist(bucketName: String, s3Client: S3Client): Boolean = try {
      Validate.notEmpty(bucketName, "The bucket name must not be null or an empty string.", "")
      val getBucketAclRequest: GetBucketAclRequest = GetBucketAclRequest.builder().bucket(bucketName).build()
      s3client.getBucketAcl(getBucketAclRequest)
      true
    } catch {
      case ase: AwsServiceException =>

        // A redirect error or an AccessDenied exception means the bucket exists but it's not in this region
        // or we don't have permissions to it.
        if ((ase.statusCode == HttpStatusCode.MOVED_PERMANENTLY) || "AccessDenied" == ase.awsErrorDetails.errorCode) return true
        if (ase.statusCode == HttpStatusCode.NOT_FOUND) return false
        throw ase
    }

    override def fetchTuloskirje(request: HttpServletRequest, hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Array[Byte]] = {
      if (!doesBucketExist(s3Settings.bucket, s3client)) {
        logger.error("Defined bucket {} does not exist.", s3Settings.bucket)
        return None
      }
      val fileSuffix = getFileSuffix(tuloskirjeKind)
      val filename = s"$hakuOid/$hakemusOid.$fileSuffix"
      val getObjectRequest: GetObjectRequest = GetObjectRequest.builder()
        .bucket(s3Settings.bucket)
        .key(filename)
        .build()
      Try(s3client.getObject(getObjectRequest, ResponseTransformer.toBytes[GetObjectResponse]())) match {
        case Success(s3Object) =>
          val content = Some(s3Object.asByteArray())
          Audit.oppija.log(FetchTuloskirje(request, hakuOid, hakemusOid))
          content
        case Failure(e: S3Exception) =>
          if (!"NoSuchKey".equals(e.awsErrorDetails().errorCode())) {
            logExceptions(e, filename)
          }
          None
        case Failure(e) =>
          logExceptions(e, filename)
          None
      }
    }

    override def getTuloskirjeInfo(request: HttpServletRequest, hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Tuloskirje] = {
      getHeadObjectResponse(hakuOid, hakemusOid, tuloskirjeKind) match {
        case Some(headObjectResponse) => Some(Tuloskirje(hakuOid, headObjectResponse.lastModified().getLong(ChronoField.MILLI_OF_SECOND)))
        case None => None
      }
    }

    private def getHeadObjectResponse(hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[HeadObjectResponse] = {
      val fileSuffix = getFileSuffix(tuloskirjeKind)
      val filename = s"$hakuOid/$hakemusOid.$fileSuffix"
      val headObjectRequest: HeadObjectRequest = HeadObjectRequest.builder()
        .bucket(s3Settings.bucket)
        .key(filename)
        .build()
      Try(s3client.headObject(headObjectRequest)) match {
        case Success(headObjectResponse) =>
          Some(headObjectResponse)
        case Failure(e: S3Exception) if e.statusCode() == 404 =>
          None
        case Failure(e) =>
          logExceptions(e, filename)
          None
      }
    }

    private def logExceptions(t: Throwable, filename: String) : Unit = {
      t match {
        case e: AwsServiceException => logger.error(s"""Got error from Amazon s3 when trying to get $filename. HTTP status code ${e.statusCode()}, AWS Error Code ${e.awsErrorDetails().errorCode()},
           error message ${e.getMessage}, error type ${e.awsErrorDetails().errorCode()}, request ID ${e.requestId()}""", e)
        case e: SdkClientException => logger.error(s"""Unable to retrieve file content or metadata from Amazon s3 for $filename. Got error message ${e.getMessage}""", e)
        case e: IOException => logger.error("Could not read content from file {}.", filename, "", e)
        case e => logger.error("Got unexpected exception when retrieving file content or metadata from Amazon s3 for file {}.", filename, "", e)
      }
    }
  }

}

trait TuloskirjeService {
  def fetchTuloskirje(request: HttpServletRequest, hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Array[Byte]]
  def getTuloskirjeInfo(request: HttpServletRequest, hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Tuloskirje]
}
