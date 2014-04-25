package fm.common.rich

import java.util.concurrent.{CancellationException, TimeoutException}
import scala.concurrent.{Await, Awaitable}
import scala.concurrent.duration.Duration

final class RichAwait(val await: Await.type) extends AnyVal {
  /**
   * Like Await.result but returns Some(...) on success and None on Timeout or Cancellation
   */
  def getResult[T](awaitable: Awaitable[T], atMost: Duration): Option[T] = try {
    Some(await.result(awaitable, atMost))
  } catch {
    case _: CancellationException => None
    case _: TimeoutException => None
  }
}