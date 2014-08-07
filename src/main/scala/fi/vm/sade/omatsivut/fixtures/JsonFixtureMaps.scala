package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.omatsivut.json.JsonFormats

object JsonFixtureMaps extends JsonFormats {
  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  def find[T](dataFile: String, key: String)(implicit mf: Manifest[T]): Option[T] = {
    val text = io.Source.fromInputStream(getClass.getResourceAsStream(dataFile)).mkString
    val parsed: JValue = parse(text).asInstanceOf[JObject]
    val found = parsed \ (key)
    found.extractOpt[T]
  }


}
