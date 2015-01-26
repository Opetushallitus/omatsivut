import org.jsoup.Jsoup
import org.specs2.mutable._
import scala.collection.JavaConverters._

case class Muistilista(otsikko: String, kieli: String, vastaanottaja: List[String], koids: List[String], captcha: String)
class MuistilistaSpec extends Specification with TestHelpers {

  val envs = Map(
    "qa" -> Map(
      "health" -> "https://testi.opintopolku.fi/omatsivut/health",
      "muistilista" -> "https://testi.opintopolku.fi/omatsivut/muistilista",
      "emailDirectory" -> "http://shibboleth2.qa.oph.ware.fi/ryhmasahkoposti-emails/"
    ),
    "systeemitesti" -> Map(
      "health" -> "https://test-oppija.oph.ware.fi/omatsivut/health",
      "muistilista" -> "https://test-oppija.oph.ware.fi/omatsivut/muistilista",
      "emailDirectory" -> "http://wp-reppu.oph.ware.fi/ryhmasahkoposti-emails/"
    )
  )

  def parseEmails(data: String) = {
    Jsoup.parse(data).select("a").iterator.asScala.toList.map(m => m.attr("href"))
  }

  def muistilistaEmail(env: Map[String, String]) = {
    val emailParams = Muistilista(s"test subject ${Math.random()}", "fi", List("foobar@example.com"), List("1.2.246.562.14.2013092410023348364157"), "foobar")

    retry(20) {
      get(env("health"))
    }

    retry(20) {
      val url: String = env("muistilista")
      postJson(url, emailParams)
    }

    retry(20) {
      val lastEmail = parseEmails(get(env("emailDirectory")).body).last
      val email = get(env("emailDirectory") + lastEmail).body
      assertContainsAll(email, emailParams.otsikko, emailParams.vastaanottaja(0))
    }

    success
  }

  "Systeemitesti (reppu)" should {
    "/omatsivut/muistilista email" in {
      muistilistaEmail(envs("systeemitesti"))
    }
  }

//  "QA" should {
//    "/omatsivut/muistilista email" in {
//      muistilistaEmail(envs("qa"))
//    }
//  }
}