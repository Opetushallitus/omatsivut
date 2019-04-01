package fi.vm.sade.omatsivut.servlet.session

import java.nio.charset.Charset

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.scalatra.json.JacksonJsonSupport
import fi.vm.sade.omatsivut.security.{AttributeNames, AuthenticationRequiringServlet, SessionService}
import fi.vm.sade.omatsivut.security.SessionInfoRetriever.getSessionInfo
import org.json4s.Formats

class SessionServlet(val sessionService: SessionService)
  extends OmatSivutServletBase
    with AuthenticationRequiringServlet with AttributeNames with JacksonJsonSupport with JsonFormats{
  private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("ddMMYY")

  get("/") {
    contentType = formats("json")
    val sessionInfo = getSessionInfo(request)
    val displayName = sessionInfo.map(_.oppijaNimi)
    val hetu = sessionInfo.map(_.hetu)

    User(
      displayName.getOrElse(""),
      parseDateFromHetu(hetu.map(_.value))
    )
  }

  def parseDateFromHetu(hetu: Option[String]): Option[LocalDate] = {
    if (hetu.isDefined) {
      val date = hetu.get.substring(0, 6)
      try {
        return Option(formatter.parseLocalDate(date))
      } catch {
        case _: Throwable => return Option.empty
      }
    }
    Option.empty
  }
}

case class User(name: String, birthDay: Option[LocalDate])
