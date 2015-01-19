package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.groupemailer.{EmailMessage, EmailRecipient, GroupEmailComponent, HtmlEmail}
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.http.UrlValueCompressor
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioComponent
import fi.vm.sade.omatsivut.localization.Translations
import fi.vm.sade.omatsivut.tarjonta.TarjontaComponent
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.template.TemplateProcessor
import org.json4s.jackson.Serialization.write

trait MuistilistaServiceComponent {
  this: KoulutusInformaatioComponent with TarjontaComponent with GroupEmailComponent =>

  def muistilistaService(language: Language.Language): MuistilistaService

  class MuistilistaService(language: Language.Language) extends JsonFormats with Logging {
    private implicit val lang = language

    def buildMail(muistiLista: Muistilista, url: StringBuffer) = {
      url + "/muistilista/" + buildUlrEncodedOidString(muistiLista.koids)
      buildMessage(muistiLista)
    }

    def sendMail(email: EmailMessage) = {
      groupEmailService.sendMailWithoutTemplate(HtmlEmail(email))
    }

    private def buildMessage(muistilista: Muistilista): EmailMessage = {
      val html = buildHtml(muistilista)
      val receivers = muistilista.vastaaanottaja.map(v => EmailRecipient("", v)).toList
      EmailMessage("omatsivut", muistilista.lahettaja.getOrElse("muistilista@opintopolku.fi"), receivers, muistilista.otsikko, html)
    }

    private def buildHtml(muistilista: Muistilista): String = {
      TemplateProcessor.processTemplate("src/main/resources/templates/emailHeaderFooter.mustache", Map(
        "subject" -> "SUBJECT",
        "body" -> buildBody(muistilista)
      ))
    }

    private def buildBody(muistilista: Muistilista) = {
      def koulutuksetList(basketItems: List[KoulutusInformaatioBasketItem]): List[String] = {
        basketItems.map((bi) => bi.applicationOptions.map((info) => s"${info.providerName} - ${info.name}")).flatten
      }

      val hakuKoulutusList = getKoulutukset(muistilista)
        .groupBy(k => k.applicationSystemName)
        .map { case (haku, basketItems) => Map(
        "haku" -> haku,
        "koulutukset" -> koulutuksetList(basketItems))
      }

      TemplateProcessor.processTemplate("src/main/resources/templates/muistilistaEmail.mustache", Map(
        "note" -> Translations.getTranslation("emailNote", "note"),
        "openLink" -> Translations.getTranslation("emailNote", "openLink"),
        "link" -> "http://reddit.com",
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
