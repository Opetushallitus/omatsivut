package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import scala.collection.JavaConversions._
import fi.vm.sade.omatsivut.OmatSivutSpringContext
import java.util
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO

object ApplicationDaoWrapper {
  private val dao = OmatSivutSpringContext.context.getBean(classOf[ApplicationDAO])

  def findByPersonOid(personOid: String): List[Hakemus] = {
    val applicationJavaObjects: List[Application] = dao.find(new Application().setPersonOid(personOid)).toList
    applicationJavaObjects.map { application =>
      Hakemus(application.getOid, application.getReceived.getTime, convertHakuToiveet(application), convertApplicationSystem(application))
    }
  }

  private def convertApplicationSystem(application: Application): Option[Haku] = application.getApplicationSystemId match {
    case "" => None
    case applicationSystemId => HakuRepository.getApplicationSystemById(applicationSystemId)
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
      val hakutoiveet: Map[String, String] = application.getPhaseAnswers("hakutoiveet").toMap
      val hakuToiveetWithEmptyValues = hakutoiveet.filterKeys(s => s.startsWith("preference")).mapValues(s => "")
      val hakutoiveetWithoutOldPreferences = hakutoiveet.filterKeys(s => !s.startsWith("preference"))
      val updatedHakutoiveet = hakutoiveetWithoutOldPreferences ++ hakuToiveetWithEmptyValues ++ getUpdates(hakemus)
      application.addVaiheenVastaukset("hakutoiveet", updatedHakutoiveet)
      dao.update(applicationQuery, application)
    }
  }

  private def getUpdates(hakemus: Hakemus): Map[String, String] = {
    hakemus.hakutoiveet.zipWithIndex.flatMap {
      (t) => t._1.map {
        (elem) => ("preference" + (t._2 + 1) + getDelimiter(elem._1) + elem._1, elem._2)
      }
    }.toMap[String, String]
  }



  private def getDelimiter(s: String) = if(s.contains("_")) "_" else "-"
}

object HakutoiveetConverter {
  def convert(toiveet: Map[String, String]): List[Map[String, String]] = {
    groupPreferences(toiveet)
      .toList
      .map(shortenNames)
      .map(convertEmptyPreferences)
      .sortBy(map => map.get("priority"))
      .map((m) => m.filterKeys { Set("priority").contains(_) == false})
  }

  private def shortenKey(v: (String, String), delimiter: Char = '-') = {
    v._1.substring(v._1.indexOf(delimiter) + 1)
  }

  private def tupleWithShortKey(v: (String, String)) = {
    if (v._1.contains("_")) (shortenKey(v, '_'), v._2) else (shortenKey(v), v._2)
  }

  private def groupPreferences(toiveet: Map[String, String]) = {
    val regex = "preference(\\d+).*".r

    toiveet.filter((key) => regex.pattern.matcher(key._1).matches()).groupBy((key) => key._1 match {
      case regex(x: String) => x
    })
  }

  private def convertEmptyPreferences(toiveet: Map[String, String]) = {
    if (toiveet.getOrElse("Koulutus-id", "").length() == 0) {
      Map("priority" -> toiveet.getOrElse("priority", ""))
    } else {
      toiveet
    }
  }

  private def shortenNames(tuple: (String, Map[String, String])) = {
    tuple._2.map(tupleWithShortKey) ++ Map("priority" -> tuple._1)
  }
}