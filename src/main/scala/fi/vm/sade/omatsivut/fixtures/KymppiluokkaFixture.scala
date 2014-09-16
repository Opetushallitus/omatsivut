package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

class KymppiluokkaFixture(dao: ApplicationDAO) extends HakemusWithDifferentAnswersFixture(TestFixture.hakemusYhteishakuKevat2014WithForeignBaseEducationId)(dao) {
  def apply {
  val answers: Answers = Map(
    "koulutustausta" ->
      Map("POHJAKOULUTUS" -> "1", "PK_PAATTOTODISTUSVUOSI" -> "2012", "KOULUTUSPAIKKA_AMMATILLISEEN_TUTKINTOON" -> "false"),
    "osaaminen" -> Map("PK_BI_10" -> "10",
      "PK_BI_VAL1" -> "Ei arvosanaa",
      "PK_BI_VAL2" -> "Ei arvosanaa",
      "PK_YH_10" -> "10",
      "PK_FY" -> "9",
      "PK_MU_VAL1" -> "Ei arvosanaa",
      "PK_MU_VAL2" -> "Ei arvosanaa",
      "PK_B22_OPPIAINE" -> "ES",
      "PK_KO_10" -> "10",
      "PK_B22_10" -> "10",
      "PK_HI_10" -> "10",
      "PK_A22_VAL1" -> "Ei arvosanaa",
      "PK_A22_VAL2" -> "Ei arvosanaa",
      "PK_A1_10" -> "10",
      "PK_B23_VAL1" -> "Ei arvosanaa",
      "PK_B23_VAL2" -> "Ei arvosanaa",
      "PK_GE_10" -> "10",
      "PK_HI_VAL2" -> "Ei arvosanaa",
      "PK_B1_VAL1" -> "Ei arvosanaa",
      "PK_B22" -> "9",
      "PK_HI_VAL1" -> "Ei arvosanaa",
      "PK_B1_VAL2" -> "Ei arvosanaa",
      "PK_B23" -> "9",
      "PK_B1_10" -> "10",
      "PK_A12_OPPIAINE" -> "EL",
      "PK_GE" -> "9",
      "PK_AI_10" -> "10",
      "PK_MA_10" -> "10",
      "PK_TE_10" -> "10",
      "PK_A22" -> "9",
      "PK_FY_VAL2" -> "Ei arvosanaa",
      "PK_FY_VAL1" -> "Ei arvosanaa",
      "PK_B2_OPPIAINE" -> "FR",
      "PK_A2_10" -> "10",
      "PK_KS_VAL2" -> "Ei arvosanaa",
      "PK_HI" -> "9",
      "PK_A1" -> "9",
      "PK_B1_OPPIAINE" -> "EN",
      "PK_A2" -> "9",
      "PK_KS_VAL1" -> "Ei arvosanaa",
      "PK_AI" -> "9",
      "PK_FY_10" -> "10",
      "PK_A22_10" -> "10",
      "PK_A12" -> "9",
      "PK_YH" -> "9",
      "PK_GE_VAL2" -> "Ei arvosanaa",
      "PK_B22_VAL2" -> "Ei arvosanaa",
      "PK_B22_VAL1" -> "Ei arvosanaa",
      "PK_GE_VAL1" -> "Ei arvosanaa",
      "PK_B23_OPPIAINE" -> "LV",
      "PK_KU_10" -> "10",
      "PK_B2" -> "9",
      "PK_KT_10" -> "10",
      "PK_KT_VAL2" -> "Ei arvosanaa",
      "PK_B1" -> "9",
      "PK_KT_VAL1" -> "Ei arvosanaa",
      "PK_KU_VAL1" -> "Ei arvosanaa",
      "PK_KU_VAL2" -> "Ei arvosanaa",
      "PK_TE" -> "9",
      "PK_A22_OPPIAINE" -> "PT",
      "PK_KS" -> "9",
      "PK_KT" -> "9",
      "PK_KU" -> "9",
      "PK_KO" -> "9",
      "PK_A12_10" -> "10",
      "PK_BI" -> "9",
      "PK_A1_OPPIAINE" -> "FI",
      "PK_A2_VAL2" -> "Ei arvosanaa",
      "PK_A2_VAL1" -> "Ei arvosanaa",
      "PK_AI_VAL2" -> "Ei arvosanaa",
      "PK_MA_VAL2" -> "Ei arvosanaa",
      "PK_AI_VAL1" -> "Ei arvosanaa",
      "PK_A1_VAL2" -> "Ei arvosanaa",
      "PK_A1_VAL1" -> "Ei arvosanaa",
      "PK_MA_VAL1" -> "Ei arvosanaa",
      "PK_B2_10" -> "10",
      "PK_KO_VAL2" -> "Ei arvosanaa",
      "PK_KO_VAL1" -> "Ei arvosanaa",
      "PK_KE" -> "9",
      "PK_AI_OPPIAINE" -> "SV",
      "PK_A2_OPPIAINE" -> "LT",
      "PK_MU" -> "9",
      "PK_KE_10" -> "10",
      "PK_YH_VAL1" -> "Ei arvosanaa",
      "PK_YH_VAL2" -> "Ei arvosanaa",
      "PK_KE_VAL1" -> "Ei arvosanaa",
      "PK_KE_VAL2" -> "Ei arvosanaa",
      "PK_LI_VAL2" -> "Ei arvosanaa",
      "PK_LI_VAL1" -> "Ei arvosanaa",
      "PK_LI_10" -> "10",
      "PK_B2_VAL1" -> "Ei arvosanaa",
      "PK_LI" -> "9",
      "PK_B2_VAL2" -> "Ei arvosanaa",
      "PK_MU_10" -> "10",
      "PK_B23_10" -> "10",
      "PK_A12_VAL2" -> "Ei arvosanaa",
      "PK_KS_10" -> "10",
      "PK_MA" -> "9",
      "PK_A12_VAL1" -> "Ei arvosanaa",
      "PK_TE_VAL1" -> "Ei arvosanaa",
      "PK_TE_VAL2" -> "Ei arvosanaa"))
    replaceAnswers(answers)
  }
}
