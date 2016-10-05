package fi.vm.sade.hakemuseditori.viestintapalvelu

import java.net.URL

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus
import fi.vm.sade.hakemuseditori.http.HttpCall
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.tarjonta.domain.{KohteenHakuaika, Hakukohde, Haku}
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.utils.slf4j.Logging
import org.apache.commons.io.IOUtils

import scala.util.Try

trait ViestintapalveluComponent {
  val viestintapalveluService: ViestintapalveluService

  class StubbedViestintapalveluService extends ViestintapalveluService with JsonFormats with Logging {
    override def fetchHakijanTuloskirjeet(hakijaOid: String) : List[Letter] = {
      List(
        Letter(1234, "1.2.246.562.5.2013080813081926341928", "hyvaksymiskirje", "application/pdf","2016-11-14T06:56:44.159+0000"),
        Letter(1234, "1.2.246.562.29.95390561488", "hyvaksymiskirje", "application/pdf","2016-04-06T06:56:44.159+0000"))
    }
    override def fetchTuloskirje(id: Long) : Option[Array[Byte]] = {
      if(id == 1234) {
        Some("1234".getBytes)
      } else {
        None
      }
    }
  }

  class RemoteViestintapalveluService(viestintapalveluUrl: String) extends ViestintapalveluService with HttpCall {
    override def fetchHakijanTuloskirjeet(hakijaOid: String) : List[Letter] = {
      val url = viestintapalveluUrl + "/luotettu/letter/list/person/" + hakijaOid
      Try(withHttpGet("Viestintapalvelu fetch tuloskirjeet", url, {_.flatMap(ViestintapalveluParser.parseLetters)}).getOrElse(List())).getOrElse(List())
    }

    override def fetchTuloskirje(id: Long) : Option[Array[Byte]] = {
      val url = viestintapalveluUrl + "/luotettu/letter/receiverLetter/" + id
      Try(IOUtils.toByteArray(new URL(url).openStream())).toOption
    }
  }

}
trait ViestintapalveluService {
  def fetchHakijanTuloskirjeet(hakijaOid: String) : List[Letter]
  def fetchTuloskirje(id: Long) : Option[Array[Byte]]
}