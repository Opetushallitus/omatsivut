package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.groupemailer.{EmailData, EmailMessage, EmailRecipient, GroupEmailComponent}
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.http.UrlValueCompressor
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioComponent
import fi.vm.sade.omatsivut.localization.Translations
import fi.vm.sade.utils.template.TemplateProcessor
import org.json4s.jackson.Serialization.write

trait MuistilistaServiceComponent {
  this: KoulutusInformaatioComponent with GroupEmailComponent =>

  def muistilistaService(language: Language.Language): MuistilistaService

  class MuistilistaService(language: Language.Language) extends JsonFormats {
    private implicit val lang = language

    def sendMail(muistiLista: Muistilista, url: StringBuffer) = {
      val email = buildMessage(muistiLista, url + "/" + buildUlrEncodedOidString(muistiLista.koids))
      val recipients = muistiLista.vastaanottaja.map(v => EmailRecipient(XssUtility.purifyFromHtml(v)))
      groupEmailService.sendMailWithoutTemplate(EmailData(email, recipients))
    }

    private def buildMessage(muistilista: Muistilista, url: String): EmailMessage = {
      val body = buildHtml(muistilista, url)
      val subject = XssUtility.purifyFromHtml(muistilista.otsikko)
      EmailMessage("omatsivut", subject, body, true)
    }

    private def buildHtml(muistilista: Muistilista, url: String): String = {
      TemplateProcessor.processTemplate("/templates/emailHeaderFooter.mustache", Map(
        "body" -> buildBody(muistilista, url)
      ))
    }

    private def buildBody(muistilista: Muistilista, url: String) = {

      val hakuKoulutusList = getKoulutukset(muistilista)
        .map { hakuItems => Map(
        "haku" -> hakuItems.applicationSystemName,
        "koulutukset" -> hakuItems.applicationOptions.map(info => s"${info.providerName} - ${info.name}"))
      }

      TemplateProcessor.processTemplate("/templates/muistilistaEmail.mustache", Map(
        "note" -> Translations.getTranslation("emailNote", "note"),
        "openLink" -> Translations.getTranslation("emailNote", "openLink"),
        "link" -> url,
        "haut" -> hakuKoulutusList,
        "noReply" -> Translations.getTranslation("emailNote", "noReply")
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
