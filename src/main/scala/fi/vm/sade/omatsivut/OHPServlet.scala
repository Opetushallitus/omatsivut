package fi.vm.sade.omatsivut

import org.scalatra._
import scalate.ScalateSupport

class OHPServlet extends OmatsivutStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

  get("/applications") {
    contentType = "application/json"
	"""[{"name": "app1"},{"name": "app2"}]"""
  }
  
}
