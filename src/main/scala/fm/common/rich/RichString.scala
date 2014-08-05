/*
 * Copyright 2014 Frugal Mechanic (http://frugalmechanic.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fm.common.rich

import fm.common.Normalize
import java.io.File
import java.math.{BigDecimal, BigInteger}
import org.apache.commons.lang3.text.WordUtils
import scala.util.matching.Regex

final class RichString(val s: String) extends AnyVal {
  /**
   * Same as String.intern but safe for use when the string is null (i.e. it just returns null)
   */
  def internOrNull: String = if (null == s) null else s.intern
  
  /**
   * If the string is blank returns None else Some(string)
   */
  def toBlankOption: Option[String] = if(new RichCharSequence(s).isNotBlank) Some(s) else None
  
  /**
   * If this string starts with the lead param then return a new string with lead stripped from the start
   * 
   * NOTE: The same functionality is available in Scala's StringOps.stripPrefix
   */
  def stripLeading(lead: String): String = if (s.startsWith(lead)) s.substring(lead.length) else s
  
  /**
   * If this string ends with the trail param then return a new string with trail stripped from the end
   * 
   * NOTE: The same functionality is available in Scala's StringOps.stripSuffix
   */
  def stripTrailing(trail: String): String = if (s.endsWith(trail)) s.substring(0, s.length - trail.length) else s
  
  /**
   * If this string does not start with the lead param then return a new string with it added to the start of the string
   * 
   * TODO: is there a better name for this?
   */
  def requireLeading(lead: String): String = if (s.startsWith(lead)) s else lead+s
  
  /**
   * If this string does not ends with the trail param then return a new string with it added to the end of the string
   * 
   * TODO: is there a better name for this?
   */
  def requireTrailing(trail: String): String = if (s.endsWith(trail)) s else s+trail
  
  
  def toBooleanOption: Option[Boolean] = try { Some(java.lang.Boolean.valueOf(s)) } catch { case _: NumberFormatException => None }
  def toByteOption:    Option[Byte]    = try { Some(java.lang.Byte.valueOf(s))    } catch { case _: NumberFormatException => None }
  def toShortOption:   Option[Short]   = try { Some(java.lang.Short.valueOf(s))   } catch { case _: NumberFormatException => None }
  def toIntOption:     Option[Int]     = try { Some(java.lang.Integer.valueOf(s)) } catch { case _: NumberFormatException => None }
  def toLongOption:    Option[Long]    = try { Some(java.lang.Long.valueOf(s))    } catch { case _: NumberFormatException => None }
  def toFloatOption:   Option[Float]   = try { Some(java.lang.Float.valueOf(s))   } catch { case _: NumberFormatException => None }
  def toDoubleOption:  Option[Double]  = try { Some(java.lang.Double.valueOf(s))  } catch { case _: NumberFormatException => None }
  
  def isBoolean: Boolean = toBooleanOption.isDefined
  def isByte:    Boolean = toByteOption.isDefined
  def isShort:   Boolean = toShortOption.isDefined
  def isInt:     Boolean = toIntOption.isDefined
  def isLong:    Boolean = toLongOption.isDefined
  def isFloat:   Boolean = toFloatOption.isDefined
  def isDouble:  Boolean = toDoubleOption.isDefined
  
  def toBigDecimalOption: Option[BigDecimal] = {
    try {
      if(s == null) None
      else Some(new BigDecimal(s))
    } catch {
      case ex: NumberFormatException => None
    }
  }
  
  def toBigDecimal: BigDecimal = toBigDecimalOption.getOrElse{ throw new NumberFormatException(s"RichString.toBigDecimal parsing error for value: $s") }
  def isBigDecimal: Boolean = toBigDecimalOption.isDefined
  def isNotBigDecimal: Boolean = !isBigDecimal

  def toBigIntegerOption: Option[BigInteger] = toBigDecimalOption.flatMap{ bd =>
    try {
      Some(bd.toBigIntegerExact())
    } catch {
      case ex: ArithmeticException => None
    }
  }
  
  def toBigInteger: BigInteger = toBigIntegerOption.getOrElse{ throw new NumberFormatException(s"RichString.toBigInteger parsing error on value: $s") }

  /** A shortcut for "new java.io.File(s)" */
  def toFile: File = new File(s)
  
  /**
   * Truncate the string to length if it is currently larger than length.
   * 
   * Note: The resulting string will not be longer than length.  (i.e the omission counts towards the length)
   * 
   * @param length The length to truncate the string to
   * @param ommission If the string is truncated then add this to the end (Note: The resulting still will be at most length)
   */
  def truncate(length: Int, omission: String = ""): String = {
    if (s.length > length) s.substring(0, length-omission.length)+omission else s
  }
  
  /** See fm.common.Normalize.lowerAlphaNumeric */
  def lowerAlphaNumeric: String = Normalize.lowerAlphanumeric(s)
  
  /** See fm.common.Normalize.name */
  def urlName: String = Normalize.urlName(s)
  
  /** See org.apache.commons.lang3.text.WordUtils.capitalize */
  def capitalizeWords: String = WordUtils.capitalize(s)
  
  /** See org.apache.commons.lang3.text.WordUtils.capitalize */
  def capitalizeWords(delimiters: Char*): String = WordUtils.capitalize(s, delimiters:_*)
  
  /** See org.apache.commons.lang3.text.WordUtils.capitalizeFully */
  def capitalizeFully: String = WordUtils.capitalizeFully(s)
  
  /** See org.apache.commons.lang3.text.WordUtils.capitalizeFully */
  def capitalizeFully(delimiters: Char*): String = WordUtils.capitalizeFully(s, delimiters:_*)
  
  def pad(length: Int, c: Char = ' '): String = rPad(length, c)

  def lPad(length: Int, c: Char = ' '): String = {
    val target = length - s.length
    if(target <= 0) s else repeat(c, target)+s
  }

  def rPad(length: Int, c: Char = ' '): String = {
    val target = length - s.length
    if(target <= 0) s else s+repeat(c, target)
  }
  
  private def repeat(c: Char, times: Int): String = {
    val arr = new Array[Char](times)
    java.util.Arrays.fill(arr, c)
    new String(arr)
  }
  
  def replaceAll(regex: Regex, replacement: String): String = regex.replaceAllIn(s, replacement)
  
  def replaceFirst(regex: Regex, replacement: String): String = regex.replaceFirstIn(s, replacement)
}