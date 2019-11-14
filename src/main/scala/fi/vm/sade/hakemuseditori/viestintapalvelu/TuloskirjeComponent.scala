package fi.vm.sade.hakemuseditori.viestintapalvelu

import java.io.{File, FileInputStream, IOException}

import com.amazonaws.{AmazonClientException, AmazonServiceException}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{AmazonS3Exception, ObjectMetadata, S3Object}
import fi.vm.sade.hakemuseditori.auditlog.{Audit, FetchTuloskirje}
import fi.vm.sade.hakemuseditori.hakemus.domain.Tuloskirje
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.slf4j.Logging
import javax.servlet.http.HttpServletRequest
import org.apache.commons.io.IOUtils

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
    private val s3client = AmazonS3ClientBuilder.standard
      .withRegion(s3Settings.region)
      .build()

    override def fetchTuloskirje(request: HttpServletRequest, hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Array[Byte]] = {
      if (!s3client.doesBucketExistV2(s3Settings.bucket)) {
        logger.error("Defined bucket {} does not exist.", s3Settings.bucket)
        return None
      }
      val fileSuffix = getFileSuffix(tuloskirjeKind)
      val filename = s"$hakuOid/$hakemusOid.$fileSuffix"
      Try(s3client.getObject(s3Settings.bucket, filename)) match {
        case Success(s3Object) =>
          val content = getContent(s3Object)
          Audit.oppija.log(FetchTuloskirje(request, hakuOid, hakemusOid))
          content
        case Failure(e) =>
          logExceptions(e, filename)
          None
      }
    }

    private def getContent(obj: S3Object): Option[Array[Byte]] = {
      Try(IOUtils.toByteArray(obj.getObjectContent)) match {
        case Success(byteArray) => Some(byteArray)
        case Failure(e) => throw e
      }
    }

    override def getTuloskirjeInfo(request: HttpServletRequest, hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[Tuloskirje] = {
      getObjectMetadata(hakuOid, hakemusOid, tuloskirjeKind) match {
        case Some(metadata) => Some(Tuloskirje(hakuOid, metadata.getLastModified.getTime))
        case None => None
      }
    }

    private def getObjectMetadata(hakuOid: String, hakemusOid: String, tuloskirjeKind: TuloskirjeKind) : Option[ObjectMetadata] = {
      val fileSuffix = getFileSuffix(tuloskirjeKind)
      val filename = s"$hakuOid/$hakemusOid.$fileSuffix"
      Try(s3client.getObjectMetadata(s3Settings.bucket, filename)) match {
        case Success(metadata) =>
          Some(metadata)
        case Failure(e: AmazonS3Exception) if e.getStatusCode == 404 =>
          None
        case Failure(e) =>
          logExceptions(e, filename)
          None
      }
    }

    private def logExceptions(t: Throwable, filename: String) : Unit = {
      t match {
        case e: AmazonServiceException => logger.error(s"""Got error from Amazon s3 when trying to get $filename. HTTP status code ${e.getStatusCode}, AWS Error Code ${e.getErrorCode},
           error message ${e.getErrorMessage}, error type ${e.getErrorType}, request ID ${e.getRequestId}""", e)
        case e: AmazonClientException => logger.error(s"""Unable to retrieve file content or metadata from Amazon s3 for $filename. Got error message ${e.getMessage}""", e)
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
