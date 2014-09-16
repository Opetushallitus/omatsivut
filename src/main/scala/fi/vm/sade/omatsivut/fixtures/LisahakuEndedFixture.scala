package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

class LisahakuEndedFixture(dao: ApplicationDAO) extends HakemusWithDifferentAnswersFixture(TestFixture.hakemusLisahaku)(dao) {
  def apply {
    val answers: Answers = Map(
      "hakutoiveet" -> Map("preference1-Koulutus-id" -> "1.2.246.562.20.78030966706")
    )
    addAnswers(answers)
  }
}
