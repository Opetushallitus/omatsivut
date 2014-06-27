package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.fixtures.FixtureUtils
import org.scalatra.CookieOptions

class UtilityServlet extends AuthCookieCreating  {
  get("/fakesession") {
    createAuthCookieResponse(() => paramOption("hetu"), CookieOptions(secure = false, path = "/"), paramOption("redirect").getOrElse("/index.html"))
  }

  put("/fixtures/apply") {
    FixtureUtils.applyFixtures()
  }
}
