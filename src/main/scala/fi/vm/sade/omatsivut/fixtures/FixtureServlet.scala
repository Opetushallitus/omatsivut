package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.omatsivut.OmatsivutStack

class FixtureServlet extends OmatsivutStack  {

  put("/apply") {
    FixtureUtils.applyFixtures()
  }
}
