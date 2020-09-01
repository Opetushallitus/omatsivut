package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.hakemuseditori.fixtures.JsonFixtureMaps
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.tarjonta.domain.Hakuaika
import fi.vm.sade.omatsivut.security.{Kieli, OnrHenkilo}

object TestFixture {
  val hakemusSyksy2015EiLukionArvosanaa = "1.2.246.562.11.00004102043"
  val hakemusNivelKesa2013WithPeruskouluBaseEducationId = "1.2.246.562.11.00000877107"
  val hakemusYhteishakuKevat2014WithForeignBaseEducationId = "1.2.246.562.11.00000441368"
  val hakemusPeruskouluWithMissingPreferences = "1.2.246.562.11.00000441373"
  val hakemusWithAtheleteQuestions = "1.2.246.562.11.00000441371"
  val hakemusLisahaku = hakemusWithAtheleteQuestions
  val hakemusWithGradeGridAndDancePreference = "1.2.246.562.11.00000855417"
  val hakemusKorkeakoulutKevat2014Id = "1.2.246.562.11.00000877687"
  val hakemusKorkeakouluYhteishakuSyksy2014Id = "1.2.246.562.11.00000877686"
  val hakemusTampereenYliopistonErillishaku = "1.2.246.562.11.00001583209"
  val hakemusErityisopetuksenaId = "1.2.246.562.11.00000877688"
  val inactiveHakemusWithApplicationRoundNotEndedId = "1.2.246.562.11.00000441369"
  val inactiveHakemusWithApplicationRoundEndedId = "1.2.246.562.11.00000441370"
  val applicationSystemNivelKesa2013Oid = "1.2.246.562.5.2014022711042555034240"
  val applicationSystemKorkeakouluSyksy2014Oid = "1.2.246.562.29.173465377510"
  val testHetu =  "010100A939R"
  val testCASticket = "ST-1594876-vedmFL1ZxqTCUDclMRyk"
  val displayName = "Erkki Esimerkki"
  val personOid =   "1.2.246.562.24.14229104472"
  val onrPerson = OnrHenkilo(
    123,
    System.currentTimeMillis(),
    false,
    true,
    Kieli(100, "fi", ""),
    Kieli(200, "sv", ""),
    "Erkki Esa",
    "Eku",
    "Esimerkki",
    testHetu,
    List(),
    "OPPIJA",
    personOid,
    "123")
  val testHetuWithNoApplications = "300794-937F"
  val testCASticketWithNoPersonOid = "ST-1594876-PBqUwSiL4mR6fcU48Q2V"
  val onrPersonNoApplications = OnrHenkilo(
    234,
    System.currentTimeMillis(),
    false,
    true,
    Kieli(100, "fi", ""),
    Kieli(200, "sv", ""),
    "Harri Hakemukseton",
    "Hasa",
    "Hassuttelija",
    testHetuWithNoApplications,
    List(),
    "OPPIJA",
    "1.2.246.562.24.79213463339",
    "234")
  val testHetuWithNoPersonOid = "091094-970D"

  val persons: Map[String, OnrHenkilo] = Map((testHetu, onrPerson),
                                             (testHetuWithNoApplications, onrPersonNoApplications))

  lazy val ammattistartti: HakutoiveData = JsonFixtureMaps.findByKey[HakutoiveData]("/hakemuseditorimockdata/hakutoiveet.json", "1.2.246.562.14.2014030415375012208392").get
  lazy val ammattistarttiAhlman: HakutoiveData = JsonFixtureMaps.findByKey[HakutoiveData]("/hakemuseditorimockdata/hakutoiveet.json", "1.2.246.562.14.2014040912353139913320").get
  lazy val hevostalous: HakutoiveData = JsonFixtureMaps.findByKey[HakutoiveData]("/hakemuseditorimockdata/hakutoiveet.json", "1.2.246.562.5.31982630126").get

  val hakemus2_hakuaika = Hakuaika("5474", 1404190831839L, 1922331631839L)
  val hakemusLisahaku_hakuaikaDefault = hakemus2_hakuaika
}
