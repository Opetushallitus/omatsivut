package fi.vm.sade.omatsivut.util

import cats.effect.IO

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

object RetryUtil extends Logging  {

  def retryWithBackoff[A](io: IO[A], maxRetries: Int, delay: FiniteDuration): IO[A] = {
    io.handleErrorWith {
      case NonFatal(e) if maxRetries > 0 =>
        logger.warn(s"Request failed, retrying in $delay... (${maxRetries - 1} retries left)", e)
        IO.sleep(delay) *> retryWithBackoff(io, maxRetries - 1, delay * 2)
      case otherError => IO.raiseError(otherError)
    }
  }
}
