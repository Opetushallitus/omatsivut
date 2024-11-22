package fi.vm.sade.hakemuseditori.json

import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.Application
import org.json4s.JsonAST.{JInt, JString}
import org.json4s.{CustomSerializer, JField, JObject}

import java.util.Date
import scala.collection.JavaConverters.{mapAsJavaMapConverter, mapAsScalaMapConverter}

class ApplicationSerializer extends CustomSerializer[Application](ser = format => ( {
  case JObject( // TODO received, updated, answers
  JField("oid", JString(oid)) ::
    JField("applicationSystemId", JString(applicationSystemId)) ::
    JField("personOid", JString(personOid)) ::
    JField("state", JString(state)) ::
    JField("answers", JObject(answers)) :: Nil
  ) =>
    val answersMap: java.util.Map[String, java.util.Map[String, String]] = answers.map {
      case JField(phase, JObject(answerData)) =>
        phase -> answerData.collect {
          case JField(answerKey, JString(answerValue)) => answerKey -> answerValue
        }.toMap.asJava
    }.toMap.asJava
    new Application(oid, state, applicationSystemId, personOid, answersMap)
}, {
  case application: Application =>
    val answers = application.getAnswers.asScala.map {
      case (phase, answerMap) =>
        JField(phase, JObject(answerMap.asScala.map {
          case (answerKey, answerValue) => JField(answerKey, JString(answerValue))
        }.toList))
    }.toList
    JObject(
      JField("oid", JString(application.getOid())),
      JField("applicationSystemId", JString(application.getApplicationSystemId())),
      JField("personOid", JString(application.getPersonOid())),
      JField("state", JString(application.getState.toString())),
      JField("answers", JObject(answers))
    )
}
))

