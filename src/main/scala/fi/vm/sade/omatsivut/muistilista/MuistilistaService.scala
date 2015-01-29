package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.groupemailer.{EmailData, EmailMessage, EmailRecipient, GroupEmailComponent}
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.http.UrlValueCompressor
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koulutusinformaatio.{KoulutusInformaatioBasketItem, KoulutusInformaatioComponent}
import fi.vm.sade.omatsivut.localization.OmatSivutTranslations
import fi.vm.sade.utils.template.TemplateProcessor
import org.json4s.jackson.Serialization.write

trait MuistilistaServiceComponent {
  this: KoulutusInformaatioComponent with GroupEmailComponent =>

  def muistilistaService(language: Language.Language): MuistilistaService

  class MuistilistaService(language: Language.Language) extends JsonFormats {
    private implicit val lang = language
    private val erikseenHaettavatHakukohteetId = "erikseenHaettavatHakukohteet"

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

      def hakuName(haku: KoulutusInformaatioBasketItem) = {
        if(erikseenHaettavatHakukohteetId.equals(haku.applicationSystemId)) {
          OmatSivutTranslations.getTranslation("emailNote", "erikseenHaettavatHakukohteet")
        }
        else {
          haku.applicationSystemName
        }
      }

      val hakuKoulutusList = getKoulutukset(muistilista)
        .map { hakuItems => Map(
          "haku" -> hakuName(hakuItems),
          "koulutukset" -> hakuItems.applicationOptions.map(info => s"${info.providerName} - ${info.name}")
        )
      }

      TemplateProcessor.processTemplate("/templates/muistilistaEmail.mustache", Map(
        "note" -> OmatSivutTranslations.getTranslation("emailNote", "note"),
        "openLink" -> OmatSivutTranslations.getTranslation("emailNote", "openLink"),
        "link" -> url,
        "haut" -> hakuKoulutusList,
        "noReply" -> OmatSivutTranslations.getTranslation("emailNote", "noReply")
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
