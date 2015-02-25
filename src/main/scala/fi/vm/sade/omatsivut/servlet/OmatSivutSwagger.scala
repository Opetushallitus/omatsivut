package fi.vm.sade.omatsivut.servlet

import org.scalatra.swagger.{ApiInfo, Swagger}

class OmatSivutSwagger extends Swagger(
	Swagger.SpecVersion,
    "0.1.0-SNAPSHOT",
    ApiInfo("omatsivut",
            "Oppijan henkilökohtainen palvelu",
            "https://opintopolku.fi/wp/fi/opintopolku/tietoa-palvelusta/",
            "verkkotoimitus_opintopolku@oph.fi",
            "EUPL 1.1 or latest approved by the European Commission",
            "http://www.osor.eu/eupl/"))

