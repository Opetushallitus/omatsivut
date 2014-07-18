package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.domain.Hakemus._
import org.json4s.JsonAST._

object JsonConverter extends JsonFormats {
  def stringyfiedAnswers(hakemus: JValue): Answers = {
    (hakemus \ "answers").map { field =>
      def toJString(v: Any) = JString(v.toString)
      def rec(v: JValue): JValue = v match {
        case JInt(i) => toJString(i)
        case JBool(b) => toJString(b)
        case JDecimal(dec) => toJString(dec)
        case JDouble(d) => toJString(d)
        case x => x
      }
      rec(field)
    }.extract[Answers]
  }
}
