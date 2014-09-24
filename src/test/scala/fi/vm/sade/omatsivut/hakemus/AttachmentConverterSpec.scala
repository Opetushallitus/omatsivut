package fi.vm.sade.omatsivut.haku

import org.specs2.mutable.Specification

import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.hakemus.AttachmentConverter
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

class AttachmentConverterSpec extends Specification {

  implicit val lang = Language.fi

  "AttachmentConverter" should {
    "Require no additional info for basic application" in {
      AttachmentConverter.requiresAdditionalInfo(applicationSystemNivelKesa2013, applicationNivelKesa2013WithPeruskouluBaseEducationApp) must_== false
    }
    section("skipped")
    "Require additional info for application with education option spesific attachments" in {
      AttachmentConverter.requiresAdditionalInfo(applicationSystemKorkeakouluSyksy2014, applicationWithApplicationOptionAttachments) must_== true
    }
    section("skipped")
  }
}
