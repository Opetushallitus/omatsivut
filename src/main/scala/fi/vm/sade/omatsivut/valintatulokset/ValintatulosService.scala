package fi.vm.sade.omatsivut.valintatulokset

import java.util.Date

import fi.vm.sade.omatsivut.http.{DefaultHttpClient, HttpRequest}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.util.Logging
import org.json4s.JsonAST.JValue

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
  def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto)
}

case class Valintatulos(hakemusOid: String, hakutoiveet: List[HakutoiveenValintatulos])

case class HakutoiveenValintatulos(hakukohdeOid: String,
                                   tarjoajaOid: String,
                                   valintatila: String,
                                   vastaanottotila: Option[String],
                                   ilmoittautumistila: Option[String],
                                   vastaanotettavuustila: String,
                                   jonosija: Option[Int],
                                   varasijojaKaytetaanAlkaen: Option[Date],
                                   varasijojaTaytetaanAsti: Option[Date],
                                   varasijanumero: Option[Int])

case class Vastaanotto(hakukohdeOid: String, tila: String, muokkaaja: String, selite: String)

trait ValintatulosServiceComponent {
  val valintatulosService: ValintatulosService
}

class NoOpValintatulosService extends ValintatulosService {
  override def getValintatulos(hakemusOid: String, hakuOid: String) = None

  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto) {}
}

class MockValintatulosService() extends ValintatulosService with JsonFormats {
  private var valintatulokset: List[Valintatulos] = Nil

  def useFixture(fixture: List[Valintatulos]) = {
    valintatulokset = fixture
  }

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    valintatulokset.find(_.hakemusOid == hakemusOid)
  }

  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto) {
    valintatulokset = valintatulokset.map { valintatulos =>
      if (valintatulos.hakemusOid == hakemusOid) {
        valintatulos.copy(hakutoiveet = valintatulos.hakutoiveet.map { hakutoive =>
          vastaanotto.tila match {
            case "VASTAANOTTANUT" =>
              (if (hakutoive.hakukohdeOid == vastaanotto.hakukohdeOid) {
                hakutoive.copy(vastaanottotila = Some("VASTAANOTTANUT"))
              } else {
                hakutoive.copy(vastaanottotila = Some("PERUUNTUNUT"))
              }).copy(vastaanotettavuustila = "EI_VASTAANOTETTAVISSA")
          }
        })
      } else {
        valintatulos
      }
    }
  }
}

class RemoteValintatulosService(valintatulosServiceUrl: String) extends ValintatulosService with JsonFormats with Logging {
  import org.json4s.jackson.JsonMethods._

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    val url = valintatulosServiceUrl + "/haku/"+hakuOid+"/hakemus/"+hakemusOid
    val request = DefaultHttpClient.httpGet(url)

    request.responseWithHeaders match {
      case (200, _, resultString) => {
        try {
          parse(resultString).extractOpt[JValue].map(_.extract[Valintatulos])
        } catch {
          case e:Exception => {
            logger.error("Error processing response from valinta-tulos-service at " + url + ", response was " + resultString, e)
            None
          }
        }
      }
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " fetching data from valinta-tulos-service at " + url)
        None
    }
  }

  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto) {
    import org.json4s.jackson.Serialization
    val url = valintatulosServiceUrl + "/haku/"+hakuOid+"/hakemus/"+hakemusOid+"/vastaanota"
    val request = DefaultHttpClient.httpPost(url, Some(Serialization.write(vastaanotto))).header("Content-type", "application/json")
    request.responseWithHeaders match {
      case (200, _, resultString) => {
        println(resultString)
        println(resultString)
      }
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " from valinta-tulos-service at " + url)
        None
    }
  }
}