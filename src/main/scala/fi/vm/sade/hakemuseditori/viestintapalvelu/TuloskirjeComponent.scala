package fi.vm.sade.hakemuseditori.viestintapalvelu

import java.io.{FileInputStream, File}

import fi.vm.sade.hakemuseditori.auditlog.{FetchTuloskirje, AuditLoggerComponent}
import fi.vm.sade.hakemuseditori.hakemus.domain.Tuloskirje
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.slf4j.Logging
import org.apache.commons.io.IOUtils

trait TuloskirjeComponent {
  this: AuditLoggerComponent  =>

  val tuloskirjeService: TuloskirjeService

  class StubbedTuloskirjeService extends TuloskirjeService with JsonFormats with Logging {
    override def fetchTuloskirje(hakuOid: String, hakemusOid: String, henkiloOid: String) : Option[Array[Byte]] = {
      logger.info(s"Get tuloskirje info for hakemus $hakemusOid")
      hakemusOid match {
        case "1.2.246.562.11.00000441369" => Some("1.2.246.562.11.00000441369_hyvaksymiskirje".getBytes)
        case _ => None
      }
    }
    override def getTuloskirjeInfo(hakuOid: String, hakemusOid: String) : Option[Tuloskirje] ={
      fetchTuloskirje(hakuOid, hakemusOid, "").map(_ => Tuloskirje(hakuOid, 1479099404159L))
    }
  }

  class SharedDirTuloskirjeService(appConfig: AppConfig) extends TuloskirjeService with Logging {
    private val fileSystemUrl = appConfig.settings.tuloskirjeetFileSystemUrl

    private def getFileName(hakuOid: String, hakemusOid: String): File = {
      new File(s"$fileSystemUrl/$hakuOid/$hakemusOid.pdf")
    }

    override def fetchTuloskirje(hakuOid: String, hakemusOid: String, personOid: String) : Option[Array[Byte]] = {
      val file = getFileName(hakuOid, hakemusOid)
      if (file.exists()) {
        val fileStream = new FileInputStream(file)
        val byteArray: Array[Byte] = IOUtils.toByteArray(fileStream)
        IOUtils.closeQuietly(fileStream)
        auditLogger.log(FetchTuloskirje(personOid, hakuOid, hakemusOid))
        Some(byteArray)
      } else {
        logger.warn("Ei löytynyt tuloskirjettä: " + file)
        None
      }
    }

    override def getTuloskirjeInfo(hakuOid: String, hakemusOid: String) : Option[Tuloskirje] = {
      val file = getFileName(hakuOid, hakemusOid)
      if (file.exists()) {
        Some(Tuloskirje(hakuOid, file.lastModified()))
      } else {
        None
      }
    }
  }

}
trait TuloskirjeService {
  def fetchTuloskirje(hakuOid: String, hakemusOid: String, personOid: String) : Option[Array[Byte]]
  def getTuloskirjeInfo(hakuOid: String, hakemusOid: String) : Option[Tuloskirje]
}
