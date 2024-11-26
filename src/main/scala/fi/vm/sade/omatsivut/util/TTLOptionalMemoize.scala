package fi.vm.sade.omatsivut.util

import org.joda.time.DateTime

class TTLOptionalMemoize[-T, +R](f: T => Option[R], name: String, lifetimeSeconds: Long, maxSize: Integer) extends (T => Option[R]) with Logging {
  private[this] val cache = TTLCache.apply[T, R](lifetimeSeconds, maxSize)
  private[this] var lastReport = new DateTime()

  def apply(x: T): Option[R] = {
    cache.get(x) match {
      case Some(existingItem) => Some(existingItem)
      case _ =>
        reportCacheStats()
        f(x) match {
          case Some(result) =>
            cache.put(x, result)
            Some(result)
          case _ => None
        }
    }
  }

  def reportCacheStats(): Unit = {
    if (lastReport.plusHours(1).isBeforeNow) {
      lastReport.synchronized {
        lastReport = new DateTime()
      }
      logger.info(s"Reporting hourly cache stats on $name cache: " + cache.stats.toString)
    }
  }
}

class TTLOptionalMemoizeNoArgs[R](f: () => Option[R], name: String, lifetimeSeconds: Long, maxSize: Integer) extends (() => Option[R]) {
  val func = new TTLOptionalMemoize( (a: Unit) => f(), name, lifetimeSeconds, maxSize)
  def apply(): Option[R] = func.apply(())
}

object TTLOptionalMemoize {
  def memoize[T](f: () => Option[T], name: String, lifetime: Long, maxSize: Integer) = new TTLOptionalMemoizeNoArgs(f, name, lifetime, maxSize)

  def memoize[T, R](f: T => Option[R], name: String, lifetime: Long, maxSize: Integer): (T => Option[R]) = new TTLOptionalMemoize(f, name, lifetime, maxSize)

  def memoize[T1, T2, R](f: (T1, T2) => Option[R], name: String, lifetime: Long, maxSize: Integer): ((T1, T2) => Option[R]) =
    Function.untupled(memoize(f.tupled, name, lifetime, maxSize))

  def memoize[T1, T2, T3, R](f: (T1, T2, T3) => Option[R], name: String, lifetime: Long, maxSize: Integer): ((T1, T2, T3) => Option[R]) =
    Function.untupled(memoize(f.tupled, name, lifetime, maxSize))

  def memoize[T1, T2, T3, T4, T5, R](f: (T1, T2, T3, T4, T5) => Option[R], name: String, lifetime: Long, maxSize: Integer): ((T1, T2, T3, T4, T5) => Option[R]) =
    Function.untupled(memoize(f.tupled, name, lifetime, maxSize))

  def Y[T, R](f: (T => Option[R]) => T => Option[R], name: String, lifetime: Long, maxSize: Integer): (T => Option[R]) = {
    lazy val yf: (T => Option[R]) = memoize(f(yf)(_), name, lifetime, maxSize)
    yf
  }
}
