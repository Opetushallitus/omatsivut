package fi.vm.sade.omatsivut.valintatulokset

import java.util.Date

import fi.vm.sade.omatsivut.http.{DefaultHttpClient}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.util.{Logging}
import org.json4s.JsonAST.JValue

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
  def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto): Boolean
}

case class Valintatulos(hakemusOid: String, hakutoiveet: List[HakutoiveenValintatulos])

case class HakutoiveenValintatulos(hakukohdeOid: String,
                                   tarjoajaOid: String,
                                   valintatila: String,
                                   vastaanottotila: Option[String],
                                   ilmoittautumistila: Option[String],
                                   vastaanotettavuustila: String,
                                   viimeisinVastaanottotilanMuutos: Option[Date],
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

  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto) = true
}


class RemoteValintatulosService(valintatulosServiceUrl: String) extends ValintatulosService with JsonFormats with Logging {
  import org.json4s.jackson.JsonMethods._

  def applyFixture(fixture: String) {
    val url = valintatulosServiceUrl + "/util/fixtures/apply?fixturename="+fixture
    DefaultHttpClient.httpPut(url).responseWithHeaders match {
      case (200, _, resultString) =>
        logger.info("Using valinta-tulos-service fixture: " + fixture)
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " applying fixtures at " + url)
    }
  }


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

  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto) = {
    import org.json4s.jackson.Serialization
    val url = valintatulosServiceUrl + "/haku/"+hakuOid+"/hakemus/"+hakemusOid+"/vastaanota"
    val request = DefaultHttpClient.httpPost(url, Some(Serialization.write(vastaanotto))).header("Content-type", "application/json")
    request.responseWithHeaders match {
      case (200, _, resultString) => {
        logger.debug("POST " + valintatulosServiceUrl + ": " + resultString)
        true
      }
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " from valinta-tulos-service at " + url)
        false
    }
  }
}