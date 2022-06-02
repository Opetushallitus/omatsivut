package fi.vm.sade.hakemuseditori.valintatulokset.domain

import org.json4s.JsonAST.{JValue, JString, JField, JObject}
import org.json4s.jackson._
import org.json4s._

import scala.util.Try

case class Vastaanotto(hakukohdeOid: String, tila: String, muokkaaja: String, selite: String)

sealed trait VastaanottoAction

case object Peru extends VastaanottoAction
case object VastaanotaSitovasti extends VastaanottoAction
case object VastaanotaSitovastiPeruAlemmat extends VastaanottoAction
case object VastaanotaEhdollisesti extends VastaanottoAction

object VastaanottoAction {
  private val valueMapping = Map(
    "Peru" -> Peru,
    "VastaanotaSitovasti" -> VastaanotaSitovasti,
    "VastaanotaSitovastiPeruAlemmat" -> VastaanotaSitovastiPeruAlemmat,
    "VastaanotaEhdollisesti" -> VastaanotaEhdollisesti)
  val values: Seq[String] = valueMapping.keysIterator.toList
  def apply(value: String): VastaanottoAction = valueMapping.getOrElse(value, {
    throw new IllegalArgumentException(s"Unknown action '$value', expected one of $values")
  })
}

class VastaanottoActionSerializer extends CustomSerializer[VastaanottoAction]((formats: Formats) => {
  def throwMappingException(json: String, cause: Option[Exception] = None) = {
    val message = s"Can't convert $json to ${classOf[VastaanottoAction].getSimpleName}."
    cause match {
      case Some(e) => throw new MappingException(s"$message : ${e.getMessage}", e)
      case None => throw new MappingException(message)
    }
  }
  ( {
    case json@JObject(JField("action", JString(action)) :: Nil) => Try(VastaanottoAction(action)).recoverWith {
      case cause: Exception => throwMappingException(compactJson(json), Some(cause)) }.get
    case json: JValue => throwMappingException(compactJson(json))
  }, {
    case x: VastaanottoAction => JObject(JField("action", JString(x.toString)))
  })
}
)
