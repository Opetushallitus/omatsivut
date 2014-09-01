package fi.vm.sade.omatsivut.memoize

class OptionalMemoize[-T, +R](f: T => Option[R]) extends (T => Option[R]) {
  import scala.collection.mutable
  private[this] val vals = mutable.Map.empty[T, R]
  def apply(x: T): Option[R] = {
    vals.get(x) match {
      case Some(existingItem) => Some(existingItem)
      case _ =>
        val result = f(x)
        result match {
          case Some(r) =>
            vals.update(x, r)
            result
          case _ => result
        }
    }
  }
}

object OptionalMemoize {
  def memoize[T, R](f: T => Option[R]): (T => Option[R]) = new OptionalMemoize(f)

  def memoize[T1, T2, R](f: (T1, T2) => Option[R]): ((T1, T2) => Option[R]) =
    Function.untupled(memoize(f.tupled))

  def memoize[T1, T2, T3, R](f: (T1, T2, T3) => Option[R]): ((T1, T2, T3) => Option[R]) =
    Function.untupled(memoize(f.tupled))

  def memoize[T1, T2, T3, T4, T5, R](f: (T1, T2, T3, T4, T5) => Option[R]): ((T1, T2, T3, T4, T5) => Option[R]) =
    Function.untupled(memoize(f.tupled))

  def Y[T, R](f: (T => Option[R]) => T => Option[R], lifetime: Long): (T => Option[R]) = {
    lazy val yf: (T => Option[R]) = memoize(f(yf)(_))
    yf
  }
}
