package fm.common.rich

import scala.util.{Failure, Success, Try}

final class RichTry[T](val self: Try[T]) extends AnyVal {
  /**
   * If this is a Failure then map the Exception to another possible exception
   */
  @inline def mapFailure(f: Throwable => Throwable): Try[T] = self match {
    case _: Success[T] => self
    case Failure(ex) => Failure(f(ex))
  }
}