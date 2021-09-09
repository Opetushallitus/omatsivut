package fi.vm.sade.hakemuseditori.hakumaksu

import java.lang.{Boolean => JBoolean}
import java.util

import com.google.common.collect.ImmutableMap
import fi.vm.sade.hakemuseditori.hakemus.HakemusSpringContext
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationPeriod
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.Types.ApplicationOptionOid
import scala.jdk.CollectionConverters._

trait HakumaksuServiceWrapper {
  def getPaymentRequirementsForApplicationOptions(answers: util.Map[String, String]): ImmutableMap[ApplicationOptionOid, JBoolean]
  def processPayment(application: Application, applicationPeriods: List[ApplicationPeriod]): Application
}

class RemoteHakumaksuServiceWrapper(val springContext: HakemusSpringContext) extends HakumaksuServiceWrapper {
  val paymentService = springContext.paymentService
  override def getPaymentRequirementsForApplicationOptions(answers: util.Map[String, String]): ImmutableMap[ApplicationOptionOid, JBoolean] = {
    paymentService.getPaymentRequirementsForApplicationOptions(answers)
  }

  override def processPayment(application: Application, applicationPeriods: List[ApplicationPeriod]): Application = {
    paymentService.processPayment(application, applicationPeriods.asJava)
  }
}

class StubbedHakumaksuServiceWrapper() extends HakumaksuServiceWrapper {
  override def getPaymentRequirementsForApplicationOptions(answers: util.Map[String, String]): ImmutableMap[ApplicationOptionOid, JBoolean] = {
    ImmutableMap.copyOf(answers.asScala.toMap.collect {
      case (_, "1.2.246.562.20.80094370907") => ApplicationOptionOid.of("1.2.246.562.20.80094370907") -> JBoolean.TRUE
      case (_, "1.2.246.562.20.99933864235") => ApplicationOptionOid.of("1.2.246.562.20.99933864235") -> JBoolean.TRUE
    }.asJava)
  }

  override def processPayment(application: Application, applicationPeriods: List[ApplicationPeriod]): Application = {
    application
  }
}

trait HakumaksuComponent {
  def hakumaksuService: HakumaksuServiceWrapper
}
