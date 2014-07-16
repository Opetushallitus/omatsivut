package fi.vm.sade.omatsivut.hakemus

import java.util

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.{EducationBackground, Hakemus, Haku}

import scala.collection.JavaConversions._
import scala.util.Try

case class ApplicationDaoWrapper(implicit val appConfig: AppConfig) {
  private val dao = appConfig.springContext.applicationDAO

  def findByPersonOid(personOid: String): List[Hakemus] = {
    val applicationJavaObjects: List[Application] = dao.find(new Application().setPersonOid(personOid)).toList
    applicationJavaObjects.map { application =>
      val koulutusTaustaAnswers: util.Map[String, String] = application.getAnswers.get("koulutustausta")
      Hakemus(
        application.getOid,
        application.getReceived.getTime,
        convertHakuToiveet(application),
        convertApplicationSystem(application),
        EducationBackground(koulutusTaustaAnswers.get("POHJAKOULUTUS"), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false))
      )
    }
  }

  private def convertApplicationSystem(application: Application): Option[Haku] = application.getApplicationSystemId match {
    case "" => None
    case applicationSystemId => HakuRepository().getApplicationSystemById(applicationSystemId)
  }

  private def convertHakuToiveet(application: Application): List[Map[String, String]] = {
    val answers: util.Map[String, util.Map[String, String]] = application.getAnswers
    val hakuToiveetData: Map[String, String] = answers.get("hakutoiveet").toMap
    HakutoiveetConverter.convert(hakuToiveetData)
  }

  def updateApplication(hakemus: Hakemus): Unit = {
    val applicationQuery: Application = new Application().setOid(hakemus.oid)
    val applicationJavaObjects: List[Application] = dao.find(applicationQuery).toList
    applicationJavaObjects.foreach { application =>
      ApplicationUpdater.update(application, hakemus)
      dao.update(applicationQuery, application)
    }
  }
}

