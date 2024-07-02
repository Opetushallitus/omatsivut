package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.Application
import fi.vm.sade.omatsivut.fixtures.TestFixture.hakemusYhteishakuKevat2014WithForeignBaseEducation

protected class PeruskouluFixture extends HakemusWithDifferentAnswersFixture(TestFixture.hakemusYhteishakuKevat2014WithForeignBaseEducationId) {
  def apply {
    val application = hakemusYhteishakuKevat2014WithForeignBaseEducation
    // vastauksiin tämä
    val answers: Answers = Map(
      "koulutustausta" ->
        Map("POHJAKOULUTUS" -> "1", "PK_PAATTOTODISTUSVUOSI" -> "2012", "KOULUTUSPAIKKA_AMMATILLISEEN_TUTKINTOON" -> "false"),
      "osaaminen" ->
        Map("PK_AI_OPPIAINE" -> "FI",
          "PK_AI" -> "9",
          "PK_AI_VAL1" -> "0",
          "PK_AI_VAL2" -> "0",
          "PK_A1_OPPIAINE" -> "SV",
          "PK_A1" -> "8",
          "PK_A1_VAL1" -> "0",
          "PK_A1_VAL2" -> "0",
          "PK_B1_OPPIAINE" -> "EN",
          "PK_B1" -> "7",
          "PK_B1_VAL1" -> "0",
          "PK_B1_VAL2" -> "0",
          "PK_MA" -> "7",
          "PK_MA_VAL1" -> "0",
          "PK_MA_VAL2" -> "0",
          "PK_BI" -> "8",
          "PK_BI_VAL1" -> "0",
          "PK_BI_VAL2" -> "0",
          "PK_GE" -> "7",
          "PK_GE_VAL1" -> "0",
          "PK_GE_VAL2" -> "0",
          "PK_FY" -> "5",
          "PK_FY_VAL1" -> "0",
          "PK_FY_VAL2" -> "0",
          "PK_KE" -> "7",
          "PK_KE_VAL1" -> "0",
          "PK_KE_VAL2" -> "0",
          "PK_TE" -> "6",
          "PK_TE_VAL1" -> "0",
          "PK_TE_VAL2" -> "0",
          "PK_KT" -> "8",
          "PK_KT_VAL1" -> "0",
          "PK_KT_VAL2" -> "0",
          "PK_HI" -> "8",
          "PK_HI_VAL1" -> "0",
          "PK_HI_VAL2" -> "0",
          "PK_YH" -> "8",
          "PK_YH_VAL1" -> "0",
          "PK_YH_VAL2" -> "0",
          "PK_MU" -> "7",
          "PK_MU_VAL1" -> "0",
          "PK_MU_VAL2" -> "0",
          "PK_KU" -> "8",
          "PK_KU_VAL1" -> "0",
          "PK_KU_VAL2" -> "0",
          "PK_KS" -> "8",
          "PK_KS_VAL1" -> "0",
          "PK_KS_VAL2" -> "0",
          "PK_LI" -> "8",
          "PK_LI_VAL1" -> "0",
          "PK_LI_VAL2" -> "0",
          "PK_KO" -> "7",
          "PK_KO_VAL1" -> "0",
          "PK_KO_VAL2" -> "0",
          "perusopetuksen_kieli" -> "FI"))
    replaceAnswers(application, answers)
  }
}
