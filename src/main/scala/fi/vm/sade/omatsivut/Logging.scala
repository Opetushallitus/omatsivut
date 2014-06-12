package fi.vm.sade.omatsivut

import org.slf4j.LoggerFactory

trait Logging {
  protected val logger = LoggerFactory.getLogger(getClass())
}
