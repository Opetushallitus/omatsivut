package fi.vm.sade.omatsivut.memoize

class TTLOptionalMemoize[-T, +R](f: T => Option[R], lifetimeSeconds: Long) extends (T => Option[R]) {
  private[this] val cache = TTLCache.apply[T, R](lifetimeSeconds, 32)
  def apply(x: T): Option[R] = {
    cache.get(x) match {
      case Some(existingItem) => Some(existingItem)
      case _ =>
        val result = f(x)
        result match {
          case Some(r) =>
            cache.put(x, r)
            result
          case _ => None
        }
    }
  }
}

class TTLOptionalMemoizeNoArgs[R](f: () => Option[R], lifetimeSeconds: Long) extends (() => Option[R]) {
  val func = new TTLOptionalMemoize( (a:Unit) => f(), lifetimeSeconds)
  def apply(): Option[R] = func.apply(())
}

object TTLOptionalMemoize {
  def memoize[T](f: () => Option[T], lifetime: Long) = new TTLOptionalMemoizeNoArgs(f, lifetime)

  def memoize[T, R](f: T => Option[R], lifetime: Long): (T => Option[R]) = new TTLOptionalMemoize(f, lifetime)

  def memoize[T1, T2, R](f: (T1, T2) => Option[R], lifetime: Long): ((T1, T2) => Option[R]) =
    Function.untupled(memoize(f.tupled, lifetime))

  def memoize[T1, T2, T3, R](f: (T1, T2, T3) => Option[R], lifetime: Long): ((T1, T2, T3) => Option[R]) =
    Function.untupled(memoize(f.tupled, lifetime))

  def memoize[T1, T2, T3, T4, T5, R](f: (T1, T2, T3, T4, T5) => Option[R], lifetime: Long): ((T1, T2, T3, T4, T5) => Option[R]) =
    Function.untupled(memoize(f.tupled, lifetime))

  def Y[T, R](f: (T => Option[R]) => T => Option[R], lifetime: Long): (T => Option[R]) = {
    lazy val yf: (T => Option[R]) = memoize(f(yf)(_), lifetime)
    yf
  }
}
