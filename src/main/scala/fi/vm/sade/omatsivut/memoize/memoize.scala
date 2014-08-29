package fi.vm.sade.omatsivut.memoize

class Memoize1[-T, +R](f: T => R, lifetimeSeconds: Long) extends (T => R) {
  case class MemoizeItem[A](item: A, validUntil: Long)
  import scala.collection.mutable
  private[this] val vals = mutable.Map.empty[T, MemoizeItem[R]]
  def apply(x: T): R = {
    vals.get(x).filter(System.currentTimeMillis() <= _.validUntil) match {
      case Some(existingItem) =>
        existingItem.item
      case _ =>
        val result = f(x)
        vals.update(x, MemoizeItem(result, System.currentTimeMillis()+lifetimeSeconds*1000))
        result
    }
  }
}

object Memoize {
  def memoize[T, R](f: T => R, lifetime: Long): (T => R) = new Memoize1(f, lifetime)

  def memoize[T1, T2, R](f: (T1, T2) => R, lifetime: Long): ((T1, T2) => R) =
    Function.untupled(memoize(f.tupled, lifetime))

  def memoize[T1, T2, T3, R](f: (T1, T2, T3) => R, lifetime: Long): ((T1, T2, T3) => R) =
    Function.untupled(memoize(f.tupled, lifetime))

  def memoize[T1, T2, T3, T4, T5, R](f: (T1, T2, T3, T4, T5) => R, lifetime: Long): ((T1, T2, T3, T4, T5) => R) =
    Function.untupled(memoize(f.tupled, lifetime))

  def Y[T, R](f: (T => R) => T => R, lifetime: Long): (T => R) = {
    lazy val yf: (T => R) = memoize(f(yf)(_), lifetime)
    yf
  }
}
