package fi.vm.sade.omatsivut.hakemus
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.hakemus.domain.{ValidationError, Hakemus, HakemusMuutos}
import fi.vm.sade.omatsivut.lomake.domain.{QuestionNode, Lomake}
import fi.vm.sade.omatsivut.tarjonta.{Hakukohde, Haku}

trait HakemusRepository {
  def updateHakemus(lomake: Lomake, haku: Haku)(hakemus: HakemusMuutos, userOid: String)(implicit lang: Language): Option[Hakemus]
  def findStoredApplicationByOid(oid: String): Application
  def fetchHakemukset(personOid: String)(implicit lang: Language): List[HakemusInfo]
  def getHakemus(personOid: String, hakemusOid: String)(implicit lang: Language): Option[HakemusInfo]
  def exists(personOid: String, hakuOid: String, hakemusOid: String): Boolean
}

case class HakemusInfo(hakemus: Hakemus, errors: List[ValidationError], questions: List[QuestionNode])
