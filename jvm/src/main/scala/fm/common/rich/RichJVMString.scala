package fm.common.rich

import fm.common.Implicits._
import java.util.Locale // not scala-js compatible

final class RichJVMString(val s: String) extends AnyVal {
  // Parse a language tag to a locale - Using Locale.Builder() instead of Locale.forLanguageTag catches malformed strings
  def toLocaleOption: Option[Locale] = scala.util.Try{ new Locale.Builder().setLanguageTag(s).build() }.toOption.filter{ _.isValid }
  def toLocale: Locale = toLocaleOption.getOrElse(throw new Exception(s"Invalid locale language tag: $s"))
}