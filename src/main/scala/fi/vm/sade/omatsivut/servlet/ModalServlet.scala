package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig

class ModalServlet(val appConfig: AppConfig) extends OmatSivutServletBase {

  before() {
    contentType ="application/javascript"
  }

  get("/*") {
"""if (document.location.hash.indexOf("skipRaamit") < 0) {
  var modal = document.getElementById("apply-modal");
  if(!modal) {
      modal = document.createElement("script");
      modal.id = "apply-modal";
      modal.src = "/oppija-raamit/js/apply-modal.js";
      document.getElementsByTagName("head")[0].appendChild(modal);
  }
}"""
  }
}
