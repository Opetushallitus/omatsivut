package fi.vm.sade.omatsivut.hakemus
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.hakemus.domain.{Hakemus, HakemusMuutos}
import fi.vm.sade.omatsivut.lomake.domain.Lomake
import fi.vm.sade.omatsivut.tarjonta.Haku

trait HakemusRepository {
  def updateHakemus(lomake: Lomake, haku: Haku)(hakemus: HakemusMuutos, userOid: String)(implicit lang: Language): Option[Hakemus]
  def findStoredApplicationByOid(oid: String): Application
  def fetchHakemukset(personOid: String)(implicit lang: Language): List[Hakemus]
  def getHakemus(personOid: String, hakemusOid: String)(implicit lang: Language): Option[Hakemus]
  def exists(personOid: String, hakuOid: String, hakemusOid: String): Boolean
}
