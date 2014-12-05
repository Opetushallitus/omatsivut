package fi.vm.sade.omatsivut.hakemus
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.hakemus.domain.{Hakemus, HakemusMuutos, ValidationError}
import fi.vm.sade.omatsivut.lomake.domain.{Lomake, QuestionNode}
import fi.vm.sade.omatsivut.tarjonta.domain.Haku

trait HakemusRepository {
  def updateHakemus(lomake: Lomake, haku: Haku)(hakemus: HakemusMuutos, userOid: String)(implicit lang: Language): Option[Hakemus]
  def fetchHakemukset(personOid: String)(implicit lang: Language): List[HakemusInfo]
  def getHakemus(personOid: String, hakemusOid: String)(implicit lang: Language): Option[HakemusInfo]
  def exists(personOid: String, hakuOid: String, hakemusOid: String): Boolean
}

trait ApplicationRepository {
  def findStoredApplicationByOid(oid: String): Option[ImmutableLegacyApplicationWrapper]
  def findStoredApplicationByPersonAndOid(personOid: String, oid: String): Option[ImmutableLegacyApplicationWrapper]
}

case class HakemusInfo(hakemus: Hakemus, errors: List[ValidationError], questions: List[QuestionNode], tulosOk: Boolean)

object HakemusInfo {
  def apply(hakemus: Hakemus, errors: List[ValidationError], questions: List[QuestionNode]): HakemusInfo = {
    HakemusInfo(hakemus, errors, questions, tulosOk = true)
  }
}