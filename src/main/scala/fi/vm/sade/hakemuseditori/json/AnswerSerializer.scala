package fi.vm.sade.hakemuseditori.json

import org.json4s._

object AnswerSerializer {
  def stringyfiedAnswers(field: JField): JField  = field match {
    case ("answers", value) =>
      ("answers", value.map { field =>
        def toJString(v: Any) = JString(v.toString)
        def rec(v: JValue): JValue = v match {
          case JInt(i) => toJString(i)
          case JBool(b) => toJString(b)
          case JDecimal(dec) => toJString(dec)
          case JDouble(d) => toJString(d)
          case x => x
        }
        rec(field)
      })
    case field => field
  }
}
