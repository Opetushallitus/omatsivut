package fi.vm.sade.omatsivut.servlet.session

import java.nio.charset.Charset

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.scalatra.json.JacksonJsonSupport
import fi.vm.sade.hakemuseditori.json.JsonFormats

class SessionServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JsonFormats with JacksonJsonSupport {
  private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("ddMMYY")

  before() {
    contentType = formats("json")
  }

  get("/reset") {
    redirectToIndex
  }

  get("/") {
    val hetu: Option[String] = Option(request.getHeader("nationalidentificationnumber"))
    val firstName: Option[String] = Option(request.getHeader("firstname"))
    val lastName: Option[String] = Option(request.getHeader("sn"))
    
    User(
      parseDisplayName(firstName, lastName),
      parseDateFromHetu(hetu)
    )
  }

  def redirectToIndex {
    val redirectUrl = if (appConfig.usesFakeAuthentication) request.getContextPath + "/index.html" else "/"
    response.redirect(redirectUrl)
  }

  def parseDateFromHetu(hetu: Option[String]): Option[LocalDate] = {
    if (hetu.isDefined) {
      val date = hetu.get.substring(0, 6)
      return Option(formatter.parseLocalDate(date))
    }
    Option.empty
  }

  def parseDisplayName(firstName: Option[String], lastName: Option[String]): String = {
    // Dekoodataan etunimet ja sukunimi manuaalisesti, koska shibboleth välittää ASCII-enkoodatut request headerit UTF-8 -merkistössä

    val ISO88591 = Charset.forName("ISO-8859-1")
    val utf8 = Charset.forName("UTF-8")
    val builder = new StringBuilder
    if (firstName.isDefined) {
      builder.append(new String(firstName.get.getBytes(ISO88591), utf8))
    }
    if (firstName.isDefined && lastName.isDefined) builder.append(" ")
    if (lastName.isDefined) {
      builder.append(new String(lastName.get.getBytes(ISO88591), utf8))
    }
    builder.toString
  }

}

case class User(name: String, birthDay: Option[LocalDate])
