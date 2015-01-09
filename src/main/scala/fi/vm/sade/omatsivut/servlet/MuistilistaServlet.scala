package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.muistilista.{MuistilistaServiceComponent, Muistilista}
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization
import org.scalatra.json.JacksonJsonSupport

trait MuistilistaServletContainer {

  this: MuistilistaServiceComponent =>

    class MuistilistaServlet extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with Logging {

      get("/:compressedMuistilista") {
        val compressedMuistilista = params("compressedMuistilista")
        //TODO: hae keksistä muistilista
      }

      post() {
        val muistiLista = Serialization.read[Muistilista](request.body)
        muistilistaService(language).buildMail(muistiLista, request.getRequestURL)
      }
    }

}
