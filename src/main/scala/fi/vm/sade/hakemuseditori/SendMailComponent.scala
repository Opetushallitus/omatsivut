package fi.vm.sade.hakemuseditori

import fi.vm.sade.hakemuseditori.hakemus.HakemusSpringContext
import fi.vm.sade.haku.oppija.hakemus.domain.Application

trait SendMailServiceWrapper {
  def sendModifiedEmail(application: Application): Unit
}

class RemoteSendMailServiceWrapper(val springContext: HakemusSpringContext) extends SendMailServiceWrapper {
  val sendMailService = springContext.sendMailService
  override def sendModifiedEmail(application: Application): Unit = {
    sendMailService.sendModifiedEmail(application)
  }
}

class StubbedSendMailServiceWrapper() extends SendMailServiceWrapper {
  override def sendModifiedEmail(application: Application): Unit = {
    println("send mail.")
  }
}

trait SendMailComponent {
  def sendMailService: SendMailServiceWrapper
}
