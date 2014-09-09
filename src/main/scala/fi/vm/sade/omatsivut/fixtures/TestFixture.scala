package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.haku.HakuConverter
import fi.vm.sade.omatsivut.haku.domain.HakuAika

import scala.collection.JavaConversions._

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem

import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.HakemusConverter

object TestFixture {
  val hakemusNivelKesa2013WithPeruskouluBaseEducationId = "1.2.246.562.11.00000877107"
  val hakemusYhteishakuKevat2014WithForeignBaseEducationId = "1.2.246.562.11.00000441368"
  val hakemusWithAtheleteQuestions = "1.2.246.562.11.00000441371"
  val hakemusLisahaku = hakemusWithAtheleteQuestions
  val hakemusWithGradeGridAndDancePreference = "1.2.246.562.11.00000855417"
  val hakemusWithHigherGradeAttachments = "1.2.246.562.11.00000877699"
  val hakemusWithApplicationOptionAttachments = "1.2.246.562.11.00000877686"
  val inactiveHakemus = "1.2.246.562.11.00000441369"
  val applicationSystemOid = "1.2.246.562.5.2014022711042555034240"
  val testHetu =  "010101-123N"
  val personOid =   "1.2.246.562.24.14229104472"
  val testHetuWithNoApplications = "300794-937F"
  val persons = Map((testHetu, personOid),
                    (testHetuWithNoApplications, "1.2.246.562.24.79213463339"))
  implicit val appConfig = new AppConfig.IT

  lazy val (as,applicationNivelKesa2013WithPeruskouluBaseEducationApp) = {
    (new AppConfig.IT).withConfig { appConfig =>
      val as = appConfig.springContext.applicationSystemService.getApplicationSystem(applicationSystemOid)
      val app = appConfig.springContext.applicationDAO.find(new Application().setOid(hakemusNivelKesa2013WithPeruskouluBaseEducationId)).toList.head
      (as, app)
    }
  }

  def applicationSystem: ApplicationSystem = as
  def haku(implicit lang: Language.Language) = HakuConverter.convertToHaku(applicationSystem)
  def hakemusMuutos(implicit lang: Language.Language) = {
    HakemusConverter.convertToHakemus(applicationSystem, haku, applicationNivelKesa2013WithPeruskouluBaseEducationApp).toHakemusMuutos
  }

  val ammattistartti: Hakutoive = JsonFixtureMaps.findByKey[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.14.2014030415375012208392").get
  val ammattistarttiAhlman: Hakutoive = JsonFixtureMaps.findByKey[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.14.2014040912353139913320").get
  val hevostalous: Hakutoive = JsonFixtureMaps.findByKey[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.5.31982630126").get

  val hakemus2_hakuaika = HakuAika(1404190831839L,4131320431839L)
  val hakemusLisahaku_hakuaikaForPreference = HakuAika(1409224751000L,2671528751000L)
  val hakemusLisahaku_hakuaikaDefault = hakemus2_hakuaika
}
