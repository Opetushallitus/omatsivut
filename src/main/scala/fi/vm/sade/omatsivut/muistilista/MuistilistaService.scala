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

trait MuistilistaServiceComponent {
  this: KoulutusInformaatioComponent with GroupEmailComponent =>

  def muistilistaService(language: Language.Language): MuistilistaService

  class MuistilistaService(language: Language.Language) extends JsonFormats with Logging {
    private implicit val lang = language

    def sendMail(muistiLista: Muistilista, url: StringBuffer) = {
      val email = buildMessage(muistiLista, url+ "/" + buildUlrEncodedOidString(muistiLista.koids))
      val recipients = muistiLista.vastaannottaja.map(v => EmailRecipient(v))
      val mail = EmailData(email, recipients)
      logger.info("mail="+mail)
      groupEmailService.sendMailWithoutTemplate(mail)
    }

    private def buildMessage(muistilista: Muistilista, url: String): EmailMessage = {
      val body = buildHtml(muistilista, url)
      EmailMessage("omatsivut", muistilista.lahettaja.getOrElse("muistilista@opintopolku.fi"), muistilista.otsikko, body, true)
    }

    private def buildHtml(muistilista: Muistilista, url: String): String = {
      TemplateProcessor.processTemplate("/templates/emailHeaderFooter.mustache", Map(
        "subject" -> "SUBJECT",
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
