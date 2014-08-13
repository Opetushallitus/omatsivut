package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.omatsivut.json.JsonFormats

object JsonFixtureMaps extends JsonFormats {
  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  def findByKey[T](dataFile: String, key: String)(implicit mf: Manifest[T]): Option[T] = find(dataFile, parsed => parsed \ key)

  def findByFieldValue[T](dataFile: String, key: String, value: String)(implicit mf: Manifest[T]): Option[T] = find(dataFile, matchByFieldValue(_, key, value))

  def matchByFieldValue(parsed: JObject, key: String, value: String): JArray = {
    JArray(parsed.children.filter(hasFieldValue(_, key, value)))
  }

  private def hasFieldValue(child: JValue, key: String, value: String): Boolean = {
    child match {
      case JObject(fields) => fields.find(field => {(field._1 == key) && (field._2.extractOrElse("") == value)}).isDefined
      case _ => false
    }
  }

  private def find[T](dataFile: String, matcher: JObject => JValue)(implicit mf: Manifest[T]): Option[T] = {
    val text = io.Source.fromInputStream(getClass.getResourceAsStream(dataFile)).mkString
    val parsed: JObject = parse(text).asInstanceOf[JObject]
    val found = matcher(parsed)
    found.extractOpt[T]
  }


}
