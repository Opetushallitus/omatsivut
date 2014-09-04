package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

case class LisahakuEndedFixture(appConfig: AppConfig) extends HakemusWithDifferentAnswersFixture(TestFixture.hakemusLisahaku)(appConfig: AppConfig) {
  def apply {
    val answers: Answers = Map(
      "hakutoiveet" -> Map("preference1-Koulutus-id" -> "1.2.246.562.20.78030966706")
    )
    addAnswers(answers)
  }
}
