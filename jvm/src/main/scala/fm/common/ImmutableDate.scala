package fm.common

import java.time.Instant
import java.util.Date
import scala.math.{Ordered, Ordering}

object ImmutableDate {
  /** lossless conversion so I think this is okay as an implicit */
  implicit def immutableDateToJavaDate(date: ImmutableDate): Date = date.toDate

  /** lossless conversion so I think this is okay as an implicit */
  implicit def javaDateToImmutableDate(date: Date): ImmutableDate = ImmutableDate(date)

  /** lossless conversion so I think this is okay as an implicit */
  implicit def immutableDateToJavaInstant(date: ImmutableDate): Instant = date.toInstant

  /** This is a lossless conversion from Date to ImmutableDate */
  def apply(date: Date): ImmutableDate = new ImmutableDate(date.getTime)

  /** This is a potentially lossy conversion from java.time.Instant to ImmutableDate */
  def apply(instant: Instant): ImmutableDate = new ImmutableDate(instant.toEpochMilli)

  implicit object ordering extends Ordering[ImmutableDate] {
    def compare(a: ImmutableDate, b: ImmutableDate): Int = a.compare(b)
  }
}

/**
 * Represents an immutable java.util.Date
 *
 * This provides an immutable milliseconds since epoch representation of a date
 * when it might be a mismatch to use the newer java.time.Instance (which represents things
 * as nanoseconds since or before epoch)
 */
final case class ImmutableDate(millis: Long) extends AnyVal with Ordered[ImmutableDate] {

  def toDate: Date = new Date(millis)
  def toInstant: Instant = Instant.ofEpochMilli(millis)

  def getTime: Long = millis

  def compare(that: ImmutableDate): Int = {
    if (millis < that.millis) -1
    else if (millis == that.millis) 0
    else 1
  }

  def +(other: ImmutableDate): ImmutableDate = ImmutableDate(millis + other.millis)
  def -(other: ImmutableDate): ImmutableDate = ImmutableDate(millis - other.millis)

  override def toString: String = toDate.toString()
}
