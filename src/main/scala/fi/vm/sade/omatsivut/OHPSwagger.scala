package fi.vm.sade.omatsivut

import org.scalatra.swagger.{JacksonSwaggerBase, Swagger}
import org.scalatra.ScalatraServlet
import org.scalatra.swagger.ApiInfo

class ResourcesApp(implicit val swagger: Swagger) extends ScalatraServlet with JacksonSwaggerBase 

class OHPSwagger extends Swagger(
	Swagger.SpecVersion,
    BuildInfo.version,
    ApiInfo("omatsivut",
            "Oppijan henkilökohtainen palvelu",
            "https://opintopolku.fi/wp/fi/opintopolku/tietoa-palvelusta/",
            "verkkotoimitus_opintopolku@oph.fi",
            "EUPL 1.1 or latest approved by the European Commission",
            "http://www.osor.eu/eupl/"))

