package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.groupemailer._
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.http.UrlValueCompressor
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioComponent
import fi.vm.sade.omatsivut.localization.Translations
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.template.TemplateProcessor
import org.json4s.jackson.Serialization.write

import scala.xml.Utility

trait MuistilistaServiceComponent {
  this: KoulutusInformaatioComponent with GroupEmailComponent =>

  def muistilistaService(language: Language.Language): MuistilistaService

  class MuistilistaService(language: Language.Language) extends JsonFormats with Logging {
    private implicit val lang = language

    def sendMail(muistiLista: Muistilista, url: StringBuffer) = {
      val email = buildMessage(muistiLista, url + "/" + buildUlrEncodedOidString(muistiLista.koids))
      val recipients = muistiLista.vastaannottaja.map(v => EmailRecipient(Utility.escape(v)))
      groupEmailService.sendMailWithoutTemplate(EmailData(email, recipients))
    }

    private def buildMessage(muistilista: Muistilista, url: String): EmailMessage = {
      val body = buildHtml(muistilista, url)
      val subject = Utility.escape(muistilista.otsikko)
      EmailMessage("omatsivut", "noreply@opintopolku.fi", subject, body, true)
    }

    private def buildHtml(muistilista: Muistilista, url: String): String = {
      TemplateProcessor.processTemplate("/templates/emailHeaderFooter.mustache", Map(
        "body" -> buildBody(muistilista, url)
      ))
    }

    private def buildBody(muistilista: Muistilista, url: String) = {
      def koulutuksetList(basketItems: List[KoulutusInformaatioBasketItem]): List[String] = {
        basketItems.map((bi) => bi.applicationOptions.map((info) => s"${info.providerName} - ${info.name}")).flatten
      }

      val hakuKoulutusList = getKoulutukset(muistilista)
        .groupBy(k => k.applicationSystemName)
        .map { case (haku, basketItems) => Map(
        "haku" -> haku,
        "koulutukset" -> koulutuksetList(basketItems))
      }

      TemplateProcessor.processTemplate("/templates/muistilistaEmail.mustache", Map(
        "note" -> Translations.getTranslation("emailNote", "note"),
        "openLink" -> Translations.getTranslation("emailNote", "openLink"),
        "link" -> url,
        "haut" -> hakuKoulutusList
      ))
    }

    private def getKoulutukset(muistilista: Muistilista): List[KoulutusInformaatioBasketItem] = {
      koulutusInformaatioService.koulutusWithHaku(muistilista.koids, muistilista.kieli) match {
        case Some(x) => x.asInstanceOf[List[KoulutusInformaatioBasketItem]]
        case _ => throw new IllegalStateException("koulutusWithHaku returned error")
      }
    }

    private def buildUlrEncodedOidString(oids: List[String]): String = {
      UrlValueCompressor.compress(write(oids))
    }

  }

}
