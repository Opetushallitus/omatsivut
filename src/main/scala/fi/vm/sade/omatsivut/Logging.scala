package fi.vm.sade.omatsivut

import org.slf4j.LoggerFactory

trait Logging {
  protected val logger = LoggerFactory.getLogger(getClass())
  protected def withErrorLogging[T](f: => T)(errorMsg: String): T = {
    try {
      f
    } catch {
      case e: Exception => {
        logger.error(errorMsg, e)
        throw e
      }
    }
  }
}
