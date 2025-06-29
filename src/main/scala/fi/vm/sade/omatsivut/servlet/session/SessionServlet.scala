package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.security.SessionInfoRetriever.getSessionInfo
import fi.vm.sade.omatsivut.security.{AttributeNames, AuthenticationRequiringServlet, SessionInfo, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.scalatra.json.JacksonJsonSupport

import scala.util.{Failure, Success, Try}

class SessionServlet(implicit val sessionService: SessionService)
  extends OmatSivutServletBase
    with AuthenticationRequiringServlet with AttributeNames with JacksonJsonSupport with JsonFormats{
  private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("ddMMYY")

  override val returnNotFoundIfNoOid = false

  get("/") {
    contentType = formats("json")
    val sessionInfo = getSessionInfo(request)
    val displayName = sessionInfo.map(_.oppijaNimi)

    User(
      displayName.getOrElse(""),
      parseDateFromSession(sessionInfo)
    )
  }

  def parseDateFromSession(session: Option[SessionInfo]): Option[LocalDate] = {
    def tryParseDate(h: String): Option[LocalDate] = {
      Try(formatter.parseLocalDate(h.substring(0, 6))) match {
        case Success(date) => Some(date)
        case Failure(exception) => {
          logger.warn(s"Unable to parse date from $session!", exception)
          None
        }
      }
    }
    session.map(_.hetu.value).flatMap(tryParseDate)
  }
}

case class User(name: String, birthDay: Option[LocalDate])
