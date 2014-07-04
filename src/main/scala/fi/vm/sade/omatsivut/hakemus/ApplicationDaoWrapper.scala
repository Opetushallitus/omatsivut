package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.it.dao.impl.ApplicationDAOMongoImpl
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import scala.collection.JavaConversions._
import fi.vm.sade.omatsivut.OmatSivutSpringContext
import java.util

object ApplicationDaoWrapper {
  def findByPersonOid(personOid: String): List[Hakemus] = {
    val dao: ApplicationDAOMongoImpl = OmatSivutSpringContext.context.getBean(classOf[ApplicationDAOMongoImpl])
    val applicationJavaObjects: List[Application] = dao.find(new Application()).toList
    applicationJavaObjects.map { application =>
      Hakemus(application.getOid, application.getReceived.getTime, convertHakuToiveet(application), None)
    }
  }

  def convertHakuToiveet(application: Application): List[Map[String, String]] = {
    val answers: util.Map[String, util.Map[String, String]] = application.getAnswers
    val hakuToiveetData: Map[String, String] = answers.get("hakutoiveet").toMap
    HakutoiveetConverter.convert(hakuToiveetData)
  }
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
    val pattern = "preference(\\d+).*".r
    toiveet.groupBy((key) => key._1 match {
      case pattern(x: String) => x
      case _ => ""
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