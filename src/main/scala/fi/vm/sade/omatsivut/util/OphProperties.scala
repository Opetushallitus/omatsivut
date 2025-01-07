package fi.vm.sade.omatsivut.util


import scala.collection.Map
import scala.jdk.CollectionConverters._

/**
 * Kopioitu scala-util reposta ja tuunattu uudempaan scala-versioon,
 * voisi korvata myöhemmin jollain standardi properties-toteutuksella
 * Extends OphProperties with scala types
 */
class OphProperties(files: String*) extends fi.vm.sade.properties.OphProperties(files:_*) {

  private def toJavaMap(map: Map[AnyRef, AnyRef]): java.util.Map[AnyRef, AnyRef] = {
    map.map {
      case (k, Some(v: AnyRef)) => k -> v
      case (k, None)            => k -> null
      case (k, v: AnyRef)       => k -> v
      case (k, v)               => k -> v
    }.asJava
  }

  private def toJavaList(seq: Seq[AnyRef]): java.util.List[AnyRef] = seq.asJava

  private def convertToJava(o: AnyRef): AnyRef = o match {
    case seq: Seq[_] =>
      toJavaList(seq.map(_.asInstanceOf[AnyRef]))
    case map: Map[_, _] =>
      toJavaMap(map.map { case (k, v) => k.asInstanceOf[AnyRef] -> v.asInstanceOf[AnyRef] })
    case cc: Product =>
      toJavaMap(caseClassToMap(cc))
    case other =>
      other
  }

  private def caseClassToMap(cc: Product): Map[AnyRef, AnyRef] = {
    cc.getClass.getDeclaredFields
      .filterNot(_.getName.contains("$outer")) // Exclude synthetic fields
      .map { field =>
        field.setAccessible(true)
        field.getName -> field.get(cc)
      }
      .toMap
  }

  override def convertParams(params: AnyRef*): Array[AnyRef] =
    params.map(convertToJava).toArray
}
