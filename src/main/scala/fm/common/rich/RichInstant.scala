package fm.common.rich

import java.time.Instant

final class RichInstant(val instant: Instant) extends AnyVal {
  def < (other: Instant): Boolean = instant.isBefore(other)
  def > (other: Instant): Boolean = instant.isAfter(other)
}