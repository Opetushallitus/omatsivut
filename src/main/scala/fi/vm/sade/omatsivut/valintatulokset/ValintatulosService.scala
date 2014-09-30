package fi.vm.sade.omatsivut.valintatulokset

import java.util.Date

import fi.vm.sade.omatsivut.http.{DefaultHttpClient, HttpRequest}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.util.Logging
import org.json4s.JsonAST.JValue

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
  def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto): Boolean
}

case class Vastaanottoaikataulu(vastaanottoEnd: Option[Date], vastaanottoBufferDays: Option[Int])

case class Valintatulos(hakemusOid: String, aikataulu: Option[Vastaanottoaikataulu], hakutoiveet: List[HakutoiveenValintatulos])

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

class MockValintatulosService() extends ValintatulosService with JsonFormats {
  private var valintatulokset: List[Valintatulos] = Nil

  def useFixture(fixture: List[Valintatulos]) = {
    valintatulokset = fixture
  }

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    valintatulokset.find(_.hakemusOid == hakemusOid)
  }


  private def processHakutoive(hakutoive: HakutoiveenValintatulos, vastaanotto: Vastaanotto, vastaanottotila: String, setOthersToCancelled: Boolean) = {
    val isMatch = hakutoive.hakukohdeOid == vastaanotto.hakukohdeOid
    val determinedVastaanottoTila = if (isMatch) vastaanottotila else "KESKEN"
    val valintaTila = if(setOthersToCancelled && !isMatch) "PERUUNTUNUT" else hakutoive.valintatila
    hakutoive.copy(vastaanottotila = Some(determinedVastaanottoTila), vastaanotettavuustila = "EI_VASTAANOTETTAVISSA", valintatila = valintaTila, viimeisinVastaanottotilanMuutos = Some(new Date()))
  }


  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto) = {
    valintatulokset = valintatulokset.map { valintatulos =>
      if (valintatulos.hakemusOid == hakemusOid) {
        valintatulos.copy(hakutoiveet = valintatulos.hakutoiveet.map { hakutoive =>
          vastaanotto.tila match {
            case "VASTAANOTTANUT" => processHakutoive(hakutoive, vastaanotto, "VASTAANOTTANUT", setOthersToCancelled = true)
            case "PERUNUT" => {
              val processed = processHakutoive(hakutoive, vastaanotto, "PERUNUT", setOthersToCancelled = true)
              processed.copy(valintatila = if (hakutoive.hakukohdeOid == vastaanotto.hakukohdeOid) "PERUNUT" else processed.valintatila)
            }
            case "EHDOLLISESTI_VASTAANOTTANUT" => processHakutoive(hakutoive, vastaanotto, "EHDOLLISESTI_VASTAANOTTANUT", setOthersToCancelled = false)
          }
        })
      } else {
        valintatulos
      }
    }
    true
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