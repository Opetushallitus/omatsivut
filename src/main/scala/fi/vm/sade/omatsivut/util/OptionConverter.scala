package fi.vm.sade.omatsivut.util
import java.util.Optional

object OptionConverter {
  def javaOptionalToScalaOption[T](optional: Optional[T]): Option[T] = {
    if (optional.isPresent) Some(optional.get)
    else None
  }
}
