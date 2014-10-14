package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.config.{AppConfig, ComponentRegistry, OmatSivutSpringContext}
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.tarjonta.Hakuaika

import scala.collection.JavaConversions._

object TestFixture {
  val hakemusNivelKesa2013WithPeruskouluBaseEducationId = "1.2.246.562.11.00000877107"
  val hakemusYhteishakuKevat2014WithForeignBaseEducationId = "1.2.246.562.11.00000441368"
  val hakemusWithAtheleteQuestions = "1.2.246.562.11.00000441371"
  val hakemusLisahaku = hakemusWithAtheleteQuestions
  val hakemusWithGradeGridAndDancePreference = "1.2.246.562.11.00000855417"
  val inactiveHakemus = "1.2.246.562.11.00000441369"
  val applicationSystemNivelKesa2013Oid = "1.2.246.562.5.2014022711042555034240"
  val applicationSystemKorkeakouluSyksy2014Oid = "1.2.246.562.29.173465377510"
  val testHetu =  "010101-123N"
  val personOid =   "1.2.246.562.24.14229104472"
  val testHetuWithNoApplications = "300794-937F"
  val testHetuWithNoPersonOid = "091094-970D"

  val persons = Map((testHetu, personOid),
                    (testHetuWithNoApplications, "1.2.246.562.24.79213463339"))
  val appConfig = new AppConfig.IT
  val componentRegistry = new ComponentRegistry(appConfig)

  lazy val (applicationSystemNivelKesa2013, applicationNivelKesa2013WithPeruskouluBaseEducationApp) = {
    withConfig(new ComponentRegistry(appConfig), { registry =>
      val springContext: OmatSivutSpringContext = registry.springContext
      val as = springContext.applicationSystemService.getApplicationSystem(applicationSystemNivelKesa2013Oid)
      val app = springContext.applicationDAO.find(new Application().setOid(hakemusNivelKesa2013WithPeruskouluBaseEducationId)).toList.head
      (as, app)
    })
  }

  def withConfig[T](registry: ComponentRegistry, f: (ComponentRegistry => T)): T = {
    registry.start
    try {
      f(registry)
    } finally {
      registry.stop
    }
  }

  def haku(implicit lang: Language.Language) = componentRegistry.tarjontaService.haku(applicationSystemNivelKesa2013Oid).get
  def hakemusMuutos(implicit lang: Language.Language) = {
    componentRegistry.hakemusConverter.convertToHakemus(applicationSystemNivelKesa2013, haku, applicationNivelKesa2013WithPeruskouluBaseEducationApp).toHakemusMuutos
  }

  val ammattistartti: Hakutoive = JsonFixtureMaps.findByKey[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.14.2014030415375012208392").get
  val ammattistarttiAhlman: Hakutoive = JsonFixtureMaps.findByKey[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.14.2014040912353139913320").get
  val hevostalous: Hakutoive = JsonFixtureMaps.findByKey[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.5.31982630126").get

  val hakemus2_hakuaika = Hakuaika("5474", 1404190831839L, 4131320431839L)
  val hakemusLisahaku_hakuaikaDefault = hakemus2_hakuaika
}
