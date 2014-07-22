package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.haku.virkailija.authentication.{Person, PersonBuilder}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.AppConfig.AppConfig

import scala.collection.JavaConversions._

case class FixtureImporter(implicit val appConfig: AppConfig) {
  private val dao = appConfig.springContext.applicationDAO

  def applyFixtures {
    MongoFixtureImporter.importJsonFixtures(appConfig.mongoTemplate)
    updateEmptySsnInApplications(TestFixture.personOid, TestFixture.testHetu)
  }

  def updateEmptySsnInApplications(personOid: String, ssn: String) {
    val queryApplication: Application = new Application().setPersonOid(personOid)
    val applicationJavaObjects: List[Application] = dao.find(queryApplication).toList
    applicationJavaObjects.map { application =>
      val allAnswers: Map[String, String] = application.getVastauksetMerged.toMap
      if (allAnswers.get(OppijaConstants.ELEMENT_ID_SOCIAL_SECURITY_NUMBER).isEmpty) {
        setHetu(application, ssn)
        dao.update(queryApplication, application)
      }
    }
  }

  private def setHetu(application: Application, ssn: String) {
    val allAnswers: Map[String, String] = application.getVastauksetMerged.toMap
    val personBuilder: PersonBuilder = PersonBuilder.start
      .setFirstNames(allAnswers.getOrElse(OppijaConstants.ELEMENT_ID_FIRST_NAMES, null))
      .setNickName(allAnswers.getOrElse(OppijaConstants.ELEMENT_ID_NICKNAME, null))
      .setLastName(allAnswers.getOrElse(OppijaConstants.ELEMENT_ID_LAST_NAME, null))
      .setSex(allAnswers.getOrElse(OppijaConstants.ELEMENT_ID_SEX, null))
      .setHomeCity(allAnswers.getOrElse(OppijaConstants.ELEMENT_ID_HOME_CITY, null))
      .setLanguage(allAnswers.getOrElse(OppijaConstants.ELEMENT_ID_LANGUAGE, null))
      .setNationality(allAnswers.getOrElse(OppijaConstants.ELEMENT_ID_NATIONALITY, null))
      .setContactLanguage(allAnswers.getOrElse(OppijaConstants.ELEMENT_ID_CONTACT_LANGUAGE, null))
      .setSocialSecurityNumber(ssn)
      .setPersonOid(application.getPersonOid).setSecurityOrder(false)
    val person: Person = personBuilder.get()
    application.modifyPersonalData(person)
  }

}
