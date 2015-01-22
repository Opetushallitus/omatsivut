package fi.vm.sade.omatsivut.muistilista

import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

object XssUtility {
  def purifyFromHtml(input: String): String = {
    Jsoup.parse(Jsoup.clean(input, Whitelist.none())).text()
  }
}
