/*
 * Copyright 2016 Frugal Mechanic (http://frugalmechanic.com)
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
package fm.common

import fm.common.Implicits._
import java.util.Arrays
import org.apache.commons.lang3.StringUtils
import scala.annotation.switch
import scala.collection.mutable.{ArrayBuffer,Builder}

object Normalize {
  /**
   * org.apache.commons.lang3.StringUtils.stripAccents
   */
  def stripAccents(s: String): String = StringUtils.stripAccents(s)
  
  /**
   * org.apache.commons.lang3.StringUtils.normalizeSpace
   */
  def normalizeSpace(s: String): String = StringUtils.normalizeSpace(s)
  
  /**
   * Replaces any non-alphanumeric characters with collapsed spaces
   */
  def lowerAlphanumericWithSpaces(s: String): String = {
    if (null == s) return ""
    
    val sb = new java.lang.StringBuilder
    
    var i: Int = 0
    var prevCh: Char = 0
    while (i < s.length) {
      val ch = stripAccent(s.charAt(i))
      if(Character.isLetterOrDigit(ch)) {
        sb.append(Character.toLowerCase(ch))
        prevCh = ch
      } else if(prevCh != ' ') {
        sb.append(' ')
        prevCh = ' '
      }
      i += 1
    }
    
    sb.toString.trim
  }
  
  /**
   * Removes any non-alphanumeric characters and strips accents (when it can be converted to a single character) - Only allocates a new string if the passed in string is not already normalized
   * 
   * Note: This logic should match reverseLowerAlphanumeric() -- EXCEPT that this implementation now only allocates if it needs to
   */
  def lowerAlphanumeric(s: String): String = lowerAlphanumericWithPositionsImpl(s, false)._1
  
  /**
   * Removes any non-alphanumeric characters and strips accents (when it can be converted to a single character) - Only allocates a new string if the passed in string is not already normalized
   * 
   * Note: This logic should match reverseLowerAlphanumeric() -- EXCEPT that this implementation now only allocates if it needs to
   */
  def lowerAlphanumericWithPositions(s: String): (String, Array[Int]) = lowerAlphanumericWithPositionsImpl(s, true)
  
  /**
   * The implementation for both lowerAlphanumeric and lowerAlphanumericWithPositions
   */
  private def lowerAlphanumericWithPositionsImpl(s: String, includePositions: Boolean): (String, Array[Int]) = {
    if (null == s) return ("", Array())
    
    var arr: Array[Char] = null // The lowerAlphanumeric chars
    var pos: Array[Int] = null // Original positions the lowerAlphanumeric chars came from
    var arrIdx: Int = 0
    
    var i: Int = 0
    while (i < s.length) {
      val ch: Char = stripAccent(s.charAt(i))
      
      if (null == arr && (!Character.isLetterOrDigit(ch) || ch != Character.toLowerCase(ch))) {
        // The original string is not normalized so we need to initialize arr and copy over everything so far
        arr = new Array[Char](s.length)
        if (includePositions) pos = makePositionsArray(s.length, i)
        
        // Copy over everything so far
        if (i > 0) {
          s.getChars(0, i, arr, 0)
          arrIdx = i
        }
      }
      
      // Normal case of building up our new string
      if (null != arr) {
        if (Character.isLetterOrDigit(ch)) {
          arr(arrIdx) = Character.toLowerCase(ch)
          if (includePositions) pos(arrIdx) = i
          arrIdx += 1
        }
      }
      
      i += 1
    }
    
    // If arr is null then the original string is already normalized
    val normalizedString: String = if (null == arr) s else new String(arr, 0, arrIdx)
    
    val normalizedPositions: Array[Int] = if (includePositions) {
      if (null == pos) {
        // If pos is null then arr was null so we just need to fill with 0..normalizedString.length
        makePositionsArray(normalizedString.length, normalizedString.length)
      } else {
        // Otherwise trim the pos array to the same length as the normalized string
        Arrays.copyOf(pos, normalizedString.length)
      }
    } else null
    
    (normalizedString, normalizedPositions)
  }
  
  // Used by lowerAlphanumericWithPositionsImpl
  private def makePositionsArray(length: Int, fillLength: Int = 0): Array[Int] = {
    val arr: Array[Int] = new Array(length)
    
    var i: Int = 0
    while (i < fillLength) {
      arr(i) = i
      i += 1
    }
    
    arr
  }
    
  /**
   * Given the original string and a normalized substring, extract the original version of the normalized substring.
   * e.g. Original: "Foo B.O.S.C.H. Bar"  Normalized: "bosch"  Result: "B.O.S.C.H."
   * 
   * Note: This logic should match lowerAlphanumeric
   */
  def reverseLowerAlphanumeric(original: String, normalized: String): Option[String] = {
    if (original.isBlank || normalized.isBlank) return None
    
    val (normalizedOriginal: String, positions: Array[Int]) = lowerAlphanumericWithPositions(original)
    
    val matchIdx: Int = normalizedOriginal.indexOf(normalized)
    
    if (matchIdx < 0) None else {
      val startIdx: Int = positions(matchIdx)
      
      var endIdx: Int = positions(matchIdx + normalized.length - 1)
      val maxEndIdx: Int = if (matchIdx + normalized.length >= normalizedOriginal.length) original.length else positions(matchIdx + normalized.length)
      
      // Take any additional non-whitespace up to the next normalized character
      while (endIdx < maxEndIdx && !Character.isWhitespace(original.charAt(endIdx))) {
        endIdx += 1
      }
      
      Some(original.substring(startIdx, endIdx))
    }
  }
  
  def lowerAlphaNumericWords(s: String): Array[String] = {
    val buf = new ArrayBuffer[String]
    lowerAlphaNumericWords(s, buf)
    buf.toArray
  }
  
  def lowerAlphaNumericWords(s: String, buf: Builder[String,_]): Unit = {
    val size: Int = s.length
    var i: Int = 0

    var sb = new StringBuilder
    
    while(i < size) {
      val ch: Char = stripAccent(s.charAt(i))
      
      // If its a valid character (alphanumeric or a dot) add it to the StringBuilder
      if (Character.isLetterOrDigit(ch) || ch == '.') {
        sb.append(Character.toLowerCase(ch))
      } else if (sb.length > 0) {
        // Otherwise we have a complete word, add it to the result buffer
        buf += sb.toString
        sb = new StringBuilder
      }
      
      i += 1
    }

    // If there is anything left in the StringBuilder, add it to the result buffer
    if (sb.length > 0) buf += sb.toString
  }

  def stripControl(s: String): String = {
    new String(s.filter{ch => !Character.isISOControl(ch) || '\t' == ch }.toArray)
  }

  def numeric(s: String): String = {
    new String(s.filter{ch => Character.isDigit(ch) || '.' == ch || '-' == ch }.toArray)
  }
  
  /** The word seperator character for urlName */
  private[this] val SepChar: Char = '-'
  
  /** These characters should be transformed into the SepChar in urlName */
  private[this] val ReplaceWithSepChars: Set[Char] = Set('_', '\\', '/', ' ')
  
  /** These characters should be expanded into words in urlName */
  private[this] val ExpandCharMap: Map[Char, String] = Map(
    '&' -> "and",
    '+' -> "plus",
    '"' -> "inch"
  )
  
  /**
   * Transform the string into something that is URL Friendly.
   */
  def urlName(raw: String): String = {
    // 2015-02-19 - This additional step to added to strip accented chars
    val s: String = stripAccents(raw)
    
    val sb = new java.lang.StringBuilder(s.length)
    var i: Int = 0
    var lastCharWasSep: Boolean = false
    
    while (i < s.length) {
      val ch: Char = s.charAt(i)
      
      if (ch == SepChar || ReplaceWithSepChars.contains(ch)) {
        if (!lastCharWasSep) {
          sb.append(SepChar)
          lastCharWasSep = true
        }
      } else if (ExpandCharMap.contains(ch)) {
        if (!lastCharWasSep) sb.append(SepChar)
        sb.append(ExpandCharMap(ch))
        sb.append(SepChar)
        lastCharWasSep = true
      } else if (Character.isLetterOrDigit(ch)) {
        sb.append(Character.toLowerCase(ch))
        lastCharWasSep = false
      }

      i += 1
    }
    
    // We can end up with a leading and/or trailing SepChar so lets remove those
    if (sb.charAt(0) == SepChar) sb.deleteCharAt(0)
    if (sb.charAt(sb.length - 1) == SepChar) sb.deleteCharAt(sb.length - 1)
    
    sb.toString
  }
  
  /**
   * Converts an ASCII Character to it's Unicode Full Width equivalent
   *  
   * scala> val a = (33 to 126).map{ _.toChar }
   * a: scala.collection.immutable.IndexedSeq[Char] = Vector(!, ", #, $, %, &, ', (, ), *, +, ,, -, ., /, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, :, ;, <, =, >, ?, @, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, [, \, ], ^, _, `, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, {, |, }, ~)
   * 
   * scala> val b = (65281 to 65374).map{ _.toChar }
   * b: scala.collection.immutable.IndexedSeq[Char] = Vector(！, ＂, ＃, ＄, ％, ＆, ＇, （, ）, ＊, ＋, ，, －, ．, ／, ０, １, ２, ３, ４, ５, ６, ７, ８, ９, ：, ；, ＜, ＝, ＞, ？, ＠, Ａ, Ｂ, Ｃ, Ｄ, Ｅ, Ｆ, Ｇ, Ｈ, Ｉ, Ｊ, Ｋ, Ｌ, Ｍ, Ｎ, Ｏ, Ｐ, Ｑ, Ｒ, Ｓ, Ｔ, Ｕ, Ｖ, Ｗ, Ｘ, Ｙ, Ｚ, ［, ＼, ］, ＾, ＿, ｀, ａ, ｂ, ｃ, ｄ, ｅ, ｆ, ｇ, ｈ, ｉ, ｊ, ｋ, ｌ, ｍ, ｎ, ｏ, ｐ, ｑ, ｒ, ｓ, ｔ, ｕ, ｖ, ｗ, ｘ, ｙ, ｚ, ｛, ｜, ｝, ～)
   * 
   * scala> (a zip b)
   * res44: scala.collection.immutable.IndexedSeq[(Char, Char)] = Vector((!,！), (",＂), (#,＃), ($,＄), (%,％), (&,＆), (',＇), ((,（), (),）), (*,＊), (+,＋), (,,，), (-,－), (.,．), (/,／), (0,０), (1,１), (2,２), (3,３), (4,４), (5,５), (6,６), (7,７), (8,８), (9,９), (:,：), (;,；), (<,＜), (=,＝), (>,＞), (?,？), (@,＠), (A,Ａ), (B,Ｂ), (C,Ｃ), (D,Ｄ), (E,Ｅ), (F,Ｆ), (G,Ｇ), (H,Ｈ), (I,Ｉ), (J,Ｊ), (K,Ｋ), (L,Ｌ), (M,Ｍ), (N,Ｎ), (O,Ｏ), (P,Ｐ), (Q,Ｑ), (R,Ｒ), (S,Ｓ), (T,Ｔ), (U,Ｕ), (V,Ｖ), (W,Ｗ), (X,Ｘ), (Y,Ｙ), (Z,Ｚ), ([,［), (\,＼), (],］), (^,＾), (_,＿), (`,｀), (a,ａ), (b,ｂ), (c,ｃ), (d,ｄ), (e,ｅ), (f,ｆ), (g,ｇ), (h,ｈ), (i,ｉ), (j,ｊ), (k,ｋ), (l,ｌ), (m,ｍ), (n,ｎ), (o,ｏ), (p,ｐ), (q,ｑ), (r,ｒ), (s,ｓ), (t,ｔ), (u,ｕ), (v,ｖ), (w,ｗ), (x,ｘ), (y,ｙ), (z,ｚ), ({,｛), (|,｜), (},｝), (~,～))
   * 
   */
  def toFullWidth(ch: Char): Char = if (ch == ' ') 12288.toChar else if (ch >= 33 && ch <= 126) (ch+65248).toChar else ch
  
  /**
   * Converts ASCII Characters in a String to their Unicode Full Width equivalent
   */
  def toFullWidth(s: String): String = s.map{ toFullWidth }
  
  /**
   * Converts Accented Characters to the Non-Accented Equivalent.
   * 
   * Note: This only works for when there is a 1 to 1 Character equivalence (i.e. it does not work for stuff like Æ which needs to expand to AE)
   */
  private def stripAccent(c: Char): Char = {
    // This is potentially more JIT friendly since the JVM should be able
    // to inline this method and will almost always hit the common case
    // of just returning the original character.  The slower path will be
    // calling stripAccentImpl()
    if (c < '\u0080') c else stripAccentImpl(c)
  }
  
  private def stripAccentImpl(c: Char): Char = {
    // These conversions are from Lucene's org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
    
    // Quick test: if it's not in range then just keep current character
    if (c < '\u0080') {
      c
    } else {
      (c: @switch) match {
        case '\u00C0' => 'A' // À  [LATIN CAPITAL LETTER A WITH GRAVE]
        case '\u00C1' => 'A' // Á  [LATIN CAPITAL LETTER A WITH ACUTE]
        case '\u00C2' => 'A' // Â  [LATIN CAPITAL LETTER A WITH CIRCUMFLEX]
        case '\u00C3' => 'A' // Ã  [LATIN CAPITAL LETTER A WITH TILDE]
        case '\u00C4' => 'A' // Ä  [LATIN CAPITAL LETTER A WITH DIAERESIS]
        case '\u00C5' => 'A' // Å  [LATIN CAPITAL LETTER A WITH RING ABOVE]
        case '\u0100' => 'A' // Ā  [LATIN CAPITAL LETTER A WITH MACRON]
        case '\u0102' => 'A' // Ă  [LATIN CAPITAL LETTER A WITH BREVE]
        case '\u0104' => 'A' // Ą  [LATIN CAPITAL LETTER A WITH OGONEK]
        case '\u018F' => 'A' // Ə  http://en.wikipedia.org/wiki/Schwa  [LATIN CAPITAL LETTER SCHWA]
        case '\u01CD' => 'A' // Ǎ  [LATIN CAPITAL LETTER A WITH CARON]
        case '\u01DE' => 'A' // Ǟ  [LATIN CAPITAL LETTER A WITH DIAERESIS AND MACRON]
        case '\u01E0' => 'A' // Ǡ  [LATIN CAPITAL LETTER A WITH DOT ABOVE AND MACRON]
        case '\u01FA' => 'A' // Ǻ  [LATIN CAPITAL LETTER A WITH RING ABOVE AND ACUTE]
        case '\u0200' => 'A' // Ȁ  [LATIN CAPITAL LETTER A WITH DOUBLE GRAVE]
        case '\u0202' => 'A' // Ȃ  [LATIN CAPITAL LETTER A WITH INVERTED BREVE]
        case '\u0226' => 'A' // Ȧ  [LATIN CAPITAL LETTER A WITH DOT ABOVE]
        case '\u023A' => 'A' // Ⱥ  [LATIN CAPITAL LETTER A WITH STROKE]
        case '\u1D00' => 'A' // ᴀ  [LATIN LETTER SMALL CAPITAL A]
        case '\u1E00' => 'A' // Ḁ  [LATIN CAPITAL LETTER A WITH RING BELOW]
        case '\u1EA0' => 'A' // Ạ  [LATIN CAPITAL LETTER A WITH DOT BELOW]
        case '\u1EA2' => 'A' // Ả  [LATIN CAPITAL LETTER A WITH HOOK ABOVE]
        case '\u1EA4' => 'A' // Ấ  [LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND ACUTE]
        case '\u1EA6' => 'A' // Ầ  [LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND GRAVE]
        case '\u1EA8' => 'A' // Ẩ  [LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND HOOK ABOVE]
        case '\u1EAA' => 'A' // Ẫ  [LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND TILDE]
        case '\u1EAC' => 'A' // Ậ  [LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND DOT BELOW]
        case '\u1EAE' => 'A' // Ắ  [LATIN CAPITAL LETTER A WITH BREVE AND ACUTE]
        case '\u1EB0' => 'A' // Ằ  [LATIN CAPITAL LETTER A WITH BREVE AND GRAVE]
        case '\u1EB2' => 'A' // Ẳ  [LATIN CAPITAL LETTER A WITH BREVE AND HOOK ABOVE]
        case '\u1EB4' => 'A' // Ẵ  [LATIN CAPITAL LETTER A WITH BREVE AND TILDE]
        case '\u1EB6' => 'A' // Ặ  [LATIN CAPITAL LETTER A WITH BREVE AND DOT BELOW]
        case '\u24B6' => 'A' // Ⓐ  [CIRCLED LATIN CAPITAL LETTER A]
        case '\uFF21' => 'A' // Ａ  [FULLWIDTH LATIN CAPITAL LETTER A]

        case '\u00E0' => 'a' // à  [LATIN SMALL LETTER A WITH GRAVE]
        case '\u00E1' => 'a' // á  [LATIN SMALL LETTER A WITH ACUTE]
        case '\u00E2' => 'a' // â  [LATIN SMALL LETTER A WITH CIRCUMFLEX]
        case '\u00E3' => 'a' // ã  [LATIN SMALL LETTER A WITH TILDE]
        case '\u00E4' => 'a' // ä  [LATIN SMALL LETTER A WITH DIAERESIS]
        case '\u00E5' => 'a' // å  [LATIN SMALL LETTER A WITH RING ABOVE]
        case '\u0101' => 'a' // ā  [LATIN SMALL LETTER A WITH MACRON]
        case '\u0103' => 'a' // ă  [LATIN SMALL LETTER A WITH BREVE]
        case '\u0105' => 'a' // ą  [LATIN SMALL LETTER A WITH OGONEK]
        case '\u01CE' => 'a' // ǎ  [LATIN SMALL LETTER A WITH CARON]
        case '\u01DF' => 'a' // ǟ  [LATIN SMALL LETTER A WITH DIAERESIS AND MACRON]
        case '\u01E1' => 'a' // ǡ  [LATIN SMALL LETTER A WITH DOT ABOVE AND MACRON]
        case '\u01FB' => 'a' // ǻ  [LATIN SMALL LETTER A WITH RING ABOVE AND ACUTE]
        case '\u0201' => 'a' // ȁ  [LATIN SMALL LETTER A WITH DOUBLE GRAVE]
        case '\u0203' => 'a' // ȃ  [LATIN SMALL LETTER A WITH INVERTED BREVE]
        case '\u0227' => 'a' // ȧ  [LATIN SMALL LETTER A WITH DOT ABOVE]
        case '\u0250' => 'a' // ɐ  [LATIN SMALL LETTER TURNED A]
        case '\u0259' => 'a' // ə  [LATIN SMALL LETTER SCHWA]
        case '\u025A' => 'a' // ɚ  [LATIN SMALL LETTER SCHWA WITH HOOK]
        case '\u1D8F' => 'a' // ᶏ  [LATIN SMALL LETTER A WITH RETROFLEX HOOK]
        case '\u1D95' => 'a' // ᶕ  [LATIN SMALL LETTER SCHWA WITH RETROFLEX HOOK]
        case '\u1E01' => 'a' // ạ  [LATIN SMALL LETTER A WITH RING BELOW]
        case '\u1E9A' => 'a' // ả  [LATIN SMALL LETTER A WITH RIGHT HALF RING]
        case '\u1EA1' => 'a' // ạ  [LATIN SMALL LETTER A WITH DOT BELOW]
        case '\u1EA3' => 'a' // ả  [LATIN SMALL LETTER A WITH HOOK ABOVE]
        case '\u1EA5' => 'a' // ấ  [LATIN SMALL LETTER A WITH CIRCUMFLEX AND ACUTE]
        case '\u1EA7' => 'a' // ầ  [LATIN SMALL LETTER A WITH CIRCUMFLEX AND GRAVE]
        case '\u1EA9' => 'a' // ẩ  [LATIN SMALL LETTER A WITH CIRCUMFLEX AND HOOK ABOVE]
        case '\u1EAB' => 'a' // ẫ  [LATIN SMALL LETTER A WITH CIRCUMFLEX AND TILDE]
        case '\u1EAD' => 'a' // ậ  [LATIN SMALL LETTER A WITH CIRCUMFLEX AND DOT BELOW]
        case '\u1EAF' => 'a' // ắ  [LATIN SMALL LETTER A WITH BREVE AND ACUTE]
        case '\u1EB1' => 'a' // ằ  [LATIN SMALL LETTER A WITH BREVE AND GRAVE]
        case '\u1EB3' => 'a' // ẳ  [LATIN SMALL LETTER A WITH BREVE AND HOOK ABOVE]
        case '\u1EB5' => 'a' // ẵ  [LATIN SMALL LETTER A WITH BREVE AND TILDE]
        case '\u1EB7' => 'a' // ặ  [LATIN SMALL LETTER A WITH BREVE AND DOT BELOW]
        case '\u2090' => 'a' // ₐ  [LATIN SUBSCRIPT SMALL LETTER A]
        case '\u2094' => 'a' // ₔ  [LATIN SUBSCRIPT SMALL LETTER SCHWA]
        case '\u24D0' => 'a' // ⓐ  [CIRCLED LATIN SMALL LETTER A]
        case '\u2C65' => 'a' // ⱥ  [LATIN SMALL LETTER A WITH STROKE]
        case '\u2C6F' => 'a' // Ɐ  [LATIN CAPITAL LETTER TURNED A]
        case '\uFF41' => 'a' // ａ  [FULLWIDTH LATIN SMALL LETTER A]

        case '\u0181' => 'B' // Ɓ  [LATIN CAPITAL LETTER B WITH HOOK]
        case '\u0182' => 'B' // Ƃ  [LATIN CAPITAL LETTER B WITH TOPBAR]
        case '\u0243' => 'B' // Ƀ  [LATIN CAPITAL LETTER B WITH STROKE]
        case '\u0299' => 'B' // ʙ  [LATIN LETTER SMALL CAPITAL B]
        case '\u1D03' => 'B' // ᴃ  [LATIN LETTER SMALL CAPITAL BARRED B]
        case '\u1E02' => 'B' // Ḃ  [LATIN CAPITAL LETTER B WITH DOT ABOVE]
        case '\u1E04' => 'B' // Ḅ  [LATIN CAPITAL LETTER B WITH DOT BELOW]
        case '\u1E06' => 'B' // Ḇ  [LATIN CAPITAL LETTER B WITH LINE BELOW]
        case '\u24B7' => 'B' // Ⓑ  [CIRCLED LATIN CAPITAL LETTER B]
        case '\uFF22' => 'B' // Ｂ  [FULLWIDTH LATIN CAPITAL LETTER B]

        case '\u0180' => 'b' // ƀ  [LATIN SMALL LETTER B WITH STROKE]
        case '\u0183' => 'b' // ƃ  [LATIN SMALL LETTER B WITH TOPBAR]
        case '\u0253' => 'b' // ɓ  [LATIN SMALL LETTER B WITH HOOK]
        case '\u1D6C' => 'b' // ᵬ  [LATIN SMALL LETTER B WITH MIDDLE TILDE]
        case '\u1D80' => 'b' // ᶀ  [LATIN SMALL LETTER B WITH PALATAL HOOK]
        case '\u1E03' => 'b' // ḃ  [LATIN SMALL LETTER B WITH DOT ABOVE]
        case '\u1E05' => 'b' // ḅ  [LATIN SMALL LETTER B WITH DOT BELOW]
        case '\u1E07' => 'b' // ḇ  [LATIN SMALL LETTER B WITH LINE BELOW]
        case '\u24D1' => 'b' // ⓑ  [CIRCLED LATIN SMALL LETTER B]
        case '\uFF42' => 'b' // ｂ  [FULLWIDTH LATIN SMALL LETTER B]

        case '\u00C7' => 'C' // Ç  [LATIN CAPITAL LETTER C WITH CEDILLA]
        case '\u0106' => 'C' // Ć  [LATIN CAPITAL LETTER C WITH ACUTE]
        case '\u0108' => 'C' // Ĉ  [LATIN CAPITAL LETTER C WITH CIRCUMFLEX]
        case '\u010A' => 'C' // Ċ  [LATIN CAPITAL LETTER C WITH DOT ABOVE]
        case '\u010C' => 'C' // Č  [LATIN CAPITAL LETTER C WITH CARON]
        case '\u0187' => 'C' // Ƈ  [LATIN CAPITAL LETTER C WITH HOOK]
        case '\u023B' => 'C' // Ȼ  [LATIN CAPITAL LETTER C WITH STROKE]
        case '\u0297' => 'C' // ʗ  [LATIN LETTER STRETCHED C]
        case '\u1D04' => 'C' // ᴄ  [LATIN LETTER SMALL CAPITAL C]
        case '\u1E08' => 'C' // Ḉ  [LATIN CAPITAL LETTER C WITH CEDILLA AND ACUTE]
        case '\u24B8' => 'C' // Ⓒ  [CIRCLED LATIN CAPITAL LETTER C]
        case '\uFF23' => 'C' // Ｃ  [FULLWIDTH LATIN CAPITAL LETTER C]

        case '\u00E7' => 'c' // ç  [LATIN SMALL LETTER C WITH CEDILLA]
        case '\u0107' => 'c' // ć  [LATIN SMALL LETTER C WITH ACUTE]
        case '\u0109' => 'c' // ĉ  [LATIN SMALL LETTER C WITH CIRCUMFLEX]
        case '\u010B' => 'c' // ċ  [LATIN SMALL LETTER C WITH DOT ABOVE]
        case '\u010D' => 'c' // č  [LATIN SMALL LETTER C WITH CARON]
        case '\u0188' => 'c' // ƈ  [LATIN SMALL LETTER C WITH HOOK]
        case '\u023C' => 'c' // ȼ  [LATIN SMALL LETTER C WITH STROKE]
        case '\u0255' => 'c' // ɕ  [LATIN SMALL LETTER C WITH CURL]
        case '\u1E09' => 'c' // ḉ  [LATIN SMALL LETTER C WITH CEDILLA AND ACUTE]
        case '\u2184' => 'c' // ↄ  [LATIN SMALL LETTER REVERSED C]
        case '\u24D2' => 'c' // ⓒ  [CIRCLED LATIN SMALL LETTER C]
        case '\uA73E' => 'c' // Ꜿ  [LATIN CAPITAL LETTER REVERSED C WITH DOT]
        case '\uA73F' => 'c' // ꜿ  [LATIN SMALL LETTER REVERSED C WITH DOT]
        case '\uFF43' => 'c' // ｃ  [FULLWIDTH LATIN SMALL LETTER C]

        case '\u00D0' => 'D' // Ð  [LATIN CAPITAL LETTER ETH]
        case '\u010E' => 'D' // Ď  [LATIN CAPITAL LETTER D WITH CARON]
        case '\u0110' => 'D' // Đ  [LATIN CAPITAL LETTER D WITH STROKE]
        case '\u0189' => 'D' // Ɖ  [LATIN CAPITAL LETTER AFRICAN D]
        case '\u018A' => 'D' // Ɗ  [LATIN CAPITAL LETTER D WITH HOOK]
        case '\u018B' => 'D' // Ƌ  [LATIN CAPITAL LETTER D WITH TOPBAR]
        case '\u1D05' => 'D' // ᴅ  [LATIN LETTER SMALL CAPITAL D]
        case '\u1D06' => 'D' // ᴆ  [LATIN LETTER SMALL CAPITAL ETH]
        case '\u1E0A' => 'D' // Ḋ  [LATIN CAPITAL LETTER D WITH DOT ABOVE]
        case '\u1E0C' => 'D' // Ḍ  [LATIN CAPITAL LETTER D WITH DOT BELOW]
        case '\u1E0E' => 'D' // Ḏ  [LATIN CAPITAL LETTER D WITH LINE BELOW]
        case '\u1E10' => 'D' // Ḑ  [LATIN CAPITAL LETTER D WITH CEDILLA]
        case '\u1E12' => 'D' // Ḓ  [LATIN CAPITAL LETTER D WITH CIRCUMFLEX BELOW]
        case '\u24B9' => 'D' // Ⓓ  [CIRCLED LATIN CAPITAL LETTER D]
        case '\uA779' => 'D' // Ꝺ  [LATIN CAPITAL LETTER INSULAR D]
        case '\uFF24' => 'D' // Ｄ  [FULLWIDTH LATIN CAPITAL LETTER D]

        case '\u00F0' => 'd' // ð  [LATIN SMALL LETTER ETH]
        case '\u010F' => 'd' // ď  [LATIN SMALL LETTER D WITH CARON]
        case '\u0111' => 'd' // đ  [LATIN SMALL LETTER D WITH STROKE]
        case '\u018C' => 'd' // ƌ  [LATIN SMALL LETTER D WITH TOPBAR]
        case '\u0221' => 'd' // ȡ  [LATIN SMALL LETTER D WITH CURL]
        case '\u0256' => 'd' // ɖ  [LATIN SMALL LETTER D WITH TAIL]
        case '\u0257' => 'd' // ɗ  [LATIN SMALL LETTER D WITH HOOK]
        case '\u1D6D' => 'd' // ᵭ  [LATIN SMALL LETTER D WITH MIDDLE TILDE]
        case '\u1D81' => 'd' // ᶁ  [LATIN SMALL LETTER D WITH PALATAL HOOK]
        case '\u1D91' => 'd' // ᶑ  [LATIN SMALL LETTER D WITH HOOK AND TAIL]
        case '\u1E0B' => 'd' // ḋ  [LATIN SMALL LETTER D WITH DOT ABOVE]
        case '\u1E0D' => 'd' // ḍ  [LATIN SMALL LETTER D WITH DOT BELOW]
        case '\u1E0F' => 'd' // ḏ  [LATIN SMALL LETTER D WITH LINE BELOW]
        case '\u1E11' => 'd' // ḑ  [LATIN SMALL LETTER D WITH CEDILLA]
        case '\u1E13' => 'd' // ḓ  [LATIN SMALL LETTER D WITH CIRCUMFLEX BELOW]
        case '\u24D3' => 'd' // ⓓ  [CIRCLED LATIN SMALL LETTER D]
        case '\uA77A' => 'd' // ꝺ  [LATIN SMALL LETTER INSULAR D]
        case '\uFF44' => 'd' // ｄ  [FULLWIDTH LATIN SMALL LETTER D]

        case '\u00C8' => 'E' // È  [LATIN CAPITAL LETTER E WITH GRAVE]
        case '\u00C9' => 'E' // É  [LATIN CAPITAL LETTER E WITH ACUTE]
        case '\u00CA' => 'E' // Ê  [LATIN CAPITAL LETTER E WITH CIRCUMFLEX]
        case '\u00CB' => 'E' // Ë  [LATIN CAPITAL LETTER E WITH DIAERESIS]
        case '\u0112' => 'E' // Ē  [LATIN CAPITAL LETTER E WITH MACRON]
        case '\u0114' => 'E' // Ĕ  [LATIN CAPITAL LETTER E WITH BREVE]
        case '\u0116' => 'E' // Ė  [LATIN CAPITAL LETTER E WITH DOT ABOVE]
        case '\u0118' => 'E' // Ę  [LATIN CAPITAL LETTER E WITH OGONEK]
        case '\u011A' => 'E' // Ě  [LATIN CAPITAL LETTER E WITH CARON]
        case '\u018E' => 'E' // Ǝ  [LATIN CAPITAL LETTER REVERSED E]
        case '\u0190' => 'E' // Ɛ  [LATIN CAPITAL LETTER OPEN E]
        case '\u0204' => 'E' // Ȅ  [LATIN CAPITAL LETTER E WITH DOUBLE GRAVE]
        case '\u0206' => 'E' // Ȇ  [LATIN CAPITAL LETTER E WITH INVERTED BREVE]
        case '\u0228' => 'E' // Ȩ  [LATIN CAPITAL LETTER E WITH CEDILLA]
        case '\u0246' => 'E' // Ɇ  [LATIN CAPITAL LETTER E WITH STROKE]
        case '\u1D07' => 'E' // ᴇ  [LATIN LETTER SMALL CAPITAL E]
        case '\u1E14' => 'E' // Ḕ  [LATIN CAPITAL LETTER E WITH MACRON AND GRAVE]
        case '\u1E16' => 'E' // Ḗ  [LATIN CAPITAL LETTER E WITH MACRON AND ACUTE]
        case '\u1E18' => 'E' // Ḙ  [LATIN CAPITAL LETTER E WITH CIRCUMFLEX BELOW]
        case '\u1E1A' => 'E' // Ḛ  [LATIN CAPITAL LETTER E WITH TILDE BELOW]
        case '\u1E1C' => 'E' // Ḝ  [LATIN CAPITAL LETTER E WITH CEDILLA AND BREVE]
        case '\u1EB8' => 'E' // Ẹ  [LATIN CAPITAL LETTER E WITH DOT BELOW]
        case '\u1EBA' => 'E' // Ẻ  [LATIN CAPITAL LETTER E WITH HOOK ABOVE]
        case '\u1EBC' => 'E' // Ẽ  [LATIN CAPITAL LETTER E WITH TILDE]
        case '\u1EBE' => 'E' // Ế  [LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND ACUTE]
        case '\u1EC0' => 'E' // Ề  [LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND GRAVE]
        case '\u1EC2' => 'E' // Ể  [LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND HOOK ABOVE]
        case '\u1EC4' => 'E' // Ễ  [LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND TILDE]
        case '\u1EC6' => 'E' // Ệ  [LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND DOT BELOW]
        case '\u24BA' => 'E' // Ⓔ  [CIRCLED LATIN CAPITAL LETTER E]
        case '\u2C7B' => 'E' // ⱻ  [LATIN LETTER SMALL CAPITAL TURNED E]
        case '\uFF25' => 'E' // Ｅ  [FULLWIDTH LATIN CAPITAL LETTER E]

        case '\u00E8' => 'e' // è  [LATIN SMALL LETTER E WITH GRAVE]
        case '\u00E9' => 'e' // é  [LATIN SMALL LETTER E WITH ACUTE]
        case '\u00EA' => 'e' // ê  [LATIN SMALL LETTER E WITH CIRCUMFLEX]
        case '\u00EB' => 'e' // ë  [LATIN SMALL LETTER E WITH DIAERESIS]
        case '\u0113' => 'e' // ē  [LATIN SMALL LETTER E WITH MACRON]
        case '\u0115' => 'e' // ĕ  [LATIN SMALL LETTER E WITH BREVE]
        case '\u0117' => 'e' // ė  [LATIN SMALL LETTER E WITH DOT ABOVE]
        case '\u0119' => 'e' // ę  [LATIN SMALL LETTER E WITH OGONEK]
        case '\u011B' => 'e' // ě  [LATIN SMALL LETTER E WITH CARON]
        case '\u01DD' => 'e' // ǝ  [LATIN SMALL LETTER TURNED E]
        case '\u0205' => 'e' // ȅ  [LATIN SMALL LETTER E WITH DOUBLE GRAVE]
        case '\u0207' => 'e' // ȇ  [LATIN SMALL LETTER E WITH INVERTED BREVE]
        case '\u0229' => 'e' // ȩ  [LATIN SMALL LETTER E WITH CEDILLA]
        case '\u0247' => 'e' // ɇ  [LATIN SMALL LETTER E WITH STROKE]
        case '\u0258' => 'e' // ɘ  [LATIN SMALL LETTER REVERSED E]
        case '\u025B' => 'e' // ɛ  [LATIN SMALL LETTER OPEN E]
        case '\u025C' => 'e' // ɜ  [LATIN SMALL LETTER REVERSED OPEN E]
        case '\u025D' => 'e' // ɝ  [LATIN SMALL LETTER REVERSED OPEN E WITH HOOK]
        case '\u025E' => 'e' // ɞ  [LATIN SMALL LETTER CLOSED REVERSED OPEN E]
        case '\u029A' => 'e' // ʚ  [LATIN SMALL LETTER CLOSED OPEN E]
        case '\u1D08' => 'e' // ᴈ  [LATIN SMALL LETTER TURNED OPEN E]
        case '\u1D92' => 'e' // ᶒ  [LATIN SMALL LETTER E WITH RETROFLEX HOOK]
        case '\u1D93' => 'e' // ᶓ  [LATIN SMALL LETTER OPEN E WITH RETROFLEX HOOK]
        case '\u1D94' => 'e' // ᶔ  [LATIN SMALL LETTER REVERSED OPEN E WITH RETROFLEX HOOK]
        case '\u1E15' => 'e' // ḕ  [LATIN SMALL LETTER E WITH MACRON AND GRAVE]
        case '\u1E17' => 'e' // ḗ  [LATIN SMALL LETTER E WITH MACRON AND ACUTE]
        case '\u1E19' => 'e' // ḙ  [LATIN SMALL LETTER E WITH CIRCUMFLEX BELOW]
        case '\u1E1B' => 'e' // ḛ  [LATIN SMALL LETTER E WITH TILDE BELOW]
        case '\u1E1D' => 'e' // ḝ  [LATIN SMALL LETTER E WITH CEDILLA AND BREVE]
        case '\u1EB9' => 'e' // ẹ  [LATIN SMALL LETTER E WITH DOT BELOW]
        case '\u1EBB' => 'e' // ẻ  [LATIN SMALL LETTER E WITH HOOK ABOVE]
        case '\u1EBD' => 'e' // ẽ  [LATIN SMALL LETTER E WITH TILDE]
        case '\u1EBF' => 'e' // ế  [LATIN SMALL LETTER E WITH CIRCUMFLEX AND ACUTE]
        case '\u1EC1' => 'e' // ề  [LATIN SMALL LETTER E WITH CIRCUMFLEX AND GRAVE]
        case '\u1EC3' => 'e' // ể  [LATIN SMALL LETTER E WITH CIRCUMFLEX AND HOOK ABOVE]
        case '\u1EC5' => 'e' // ễ  [LATIN SMALL LETTER E WITH CIRCUMFLEX AND TILDE]
        case '\u1EC7' => 'e' // ệ  [LATIN SMALL LETTER E WITH CIRCUMFLEX AND DOT BELOW]
        case '\u2091' => 'e' // ₑ  [LATIN SUBSCRIPT SMALL LETTER E]
        case '\u24D4' => 'e' // ⓔ  [CIRCLED LATIN SMALL LETTER E]
        case '\u2C78' => 'e' // ⱸ  [LATIN SMALL LETTER E WITH NOTCH]
        case '\uFF45' => 'e' // ｅ  [FULLWIDTH LATIN SMALL LETTER E]

        case '\u0191' => 'F' // Ƒ  [LATIN CAPITAL LETTER F WITH HOOK]
        case '\u1E1E' => 'F' // Ḟ  [LATIN CAPITAL LETTER F WITH DOT ABOVE]
        case '\u24BB' => 'F' // Ⓕ  [CIRCLED LATIN CAPITAL LETTER F]
        case '\uA730' => 'F' // ꜰ  [LATIN LETTER SMALL CAPITAL F]
        case '\uA77B' => 'F' // Ꝼ  [LATIN CAPITAL LETTER INSULAR F]
        case '\uA7FB' => 'F' // ꟻ  [LATIN EPIGRAPHIC LETTER REVERSED F]
        case '\uFF26' => 'F' // Ｆ  [FULLWIDTH LATIN CAPITAL LETTER F]

        case '\u0192' => 'f' // ƒ  [LATIN SMALL LETTER F WITH HOOK]
        case '\u1D6E' => 'f' // ᵮ  [LATIN SMALL LETTER F WITH MIDDLE TILDE]
        case '\u1D82' => 'f' // ᶂ  [LATIN SMALL LETTER F WITH PALATAL HOOK]
        case '\u1E1F' => 'f' // ḟ  [LATIN SMALL LETTER F WITH DOT ABOVE]
        case '\u1E9B' => 'f' // ẛ  [LATIN SMALL LETTER LONG S WITH DOT ABOVE]
        case '\u24D5' => 'f' // ⓕ  [CIRCLED LATIN SMALL LETTER F]
        case '\uA77C' => 'f' // ꝼ  [LATIN SMALL LETTER INSULAR F]
        case '\uFF46' => 'f' // ｆ  [FULLWIDTH LATIN SMALL LETTER F]

        case '\u011C' => 'G' // Ĝ  [LATIN CAPITAL LETTER G WITH CIRCUMFLEX]
        case '\u011E' => 'G' // Ğ  [LATIN CAPITAL LETTER G WITH BREVE]
        case '\u0120' => 'G' // Ġ  [LATIN CAPITAL LETTER G WITH DOT ABOVE]
        case '\u0122' => 'G' // Ģ  [LATIN CAPITAL LETTER G WITH CEDILLA]
        case '\u0193' => 'G' // Ɠ  [LATIN CAPITAL LETTER G WITH HOOK]
        case '\u01E4' => 'G' // Ǥ  [LATIN CAPITAL LETTER G WITH STROKE]
        case '\u01E5' => 'G' // ǥ  [LATIN SMALL LETTER G WITH STROKE]
        case '\u01E6' => 'G' // Ǧ  [LATIN CAPITAL LETTER G WITH CARON]
        case '\u01E7' => 'G' // ǧ  [LATIN SMALL LETTER G WITH CARON]
        case '\u01F4' => 'G' // Ǵ  [LATIN CAPITAL LETTER G WITH ACUTE]
        case '\u0262' => 'G' // ɢ  [LATIN LETTER SMALL CAPITAL G]
        case '\u029B' => 'G' // ʛ  [LATIN LETTER SMALL CAPITAL G WITH HOOK]
        case '\u1E20' => 'G' // Ḡ  [LATIN CAPITAL LETTER G WITH MACRON]
        case '\u24BC' => 'G' // Ⓖ  [CIRCLED LATIN CAPITAL LETTER G]
        case '\uA77D' => 'G' // Ᵹ  [LATIN CAPITAL LETTER INSULAR G]
        case '\uA77E' => 'G' // Ꝿ  [LATIN CAPITAL LETTER TURNED INSULAR G]
        case '\uFF27' => 'G' // Ｇ  [FULLWIDTH LATIN CAPITAL LETTER G]

        case '\u011D' => 'g' // ĝ  [LATIN SMALL LETTER G WITH CIRCUMFLEX]
        case '\u011F' => 'g' // ğ  [LATIN SMALL LETTER G WITH BREVE]
        case '\u0121' => 'g' // ġ  [LATIN SMALL LETTER G WITH DOT ABOVE]
        case '\u0123' => 'g' // ģ  [LATIN SMALL LETTER G WITH CEDILLA]
        case '\u01F5' => 'g' // ǵ  [LATIN SMALL LETTER G WITH ACUTE]
        case '\u0260' => 'g' // ɠ  [LATIN SMALL LETTER G WITH HOOK]
        case '\u0261' => 'g' // ɡ  [LATIN SMALL LETTER SCRIPT G]
        case '\u1D77' => 'g' // ᵷ  [LATIN SMALL LETTER TURNED G]
        case '\u1D79' => 'g' // ᵹ  [LATIN SMALL LETTER INSULAR G]
        case '\u1D83' => 'g' // ᶃ  [LATIN SMALL LETTER G WITH PALATAL HOOK]
        case '\u1E21' => 'g' // ḡ  [LATIN SMALL LETTER G WITH MACRON]
        case '\u24D6' => 'g' // ⓖ  [CIRCLED LATIN SMALL LETTER G]
        case '\uA77F' => 'g' // ꝿ  [LATIN SMALL LETTER TURNED INSULAR G]
        case '\uFF47' => 'g' // ｇ  [FULLWIDTH LATIN SMALL LETTER G]

        case '\u0124' => 'H' // Ĥ  [LATIN CAPITAL LETTER H WITH CIRCUMFLEX]
        case '\u0126' => 'H' // Ħ  [LATIN CAPITAL LETTER H WITH STROKE]
        case '\u021E' => 'H' // Ȟ  [LATIN CAPITAL LETTER H WITH CARON]
        case '\u029C' => 'H' // ʜ  [LATIN LETTER SMALL CAPITAL H]
        case '\u1E22' => 'H' // Ḣ  [LATIN CAPITAL LETTER H WITH DOT ABOVE]
        case '\u1E24' => 'H' // Ḥ  [LATIN CAPITAL LETTER H WITH DOT BELOW]
        case '\u1E26' => 'H' // Ḧ  [LATIN CAPITAL LETTER H WITH DIAERESIS]
        case '\u1E28' => 'H' // Ḩ  [LATIN CAPITAL LETTER H WITH CEDILLA]
        case '\u1E2A' => 'H' // Ḫ  [LATIN CAPITAL LETTER H WITH BREVE BELOW]
        case '\u24BD' => 'H' // Ⓗ  [CIRCLED LATIN CAPITAL LETTER H]
        case '\u2C67' => 'H' // Ⱨ  [LATIN CAPITAL LETTER H WITH DESCENDER]
        case '\u2C75' => 'H' // Ⱶ  [LATIN CAPITAL LETTER HALF H]
        case '\uFF28' => 'H' // Ｈ  [FULLWIDTH LATIN CAPITAL LETTER H]

        case '\u0125' => 'h' // ĥ  [LATIN SMALL LETTER H WITH CIRCUMFLEX]
        case '\u0127' => 'h' // ħ  [LATIN SMALL LETTER H WITH STROKE]
        case '\u021F' => 'h' // ȟ  [LATIN SMALL LETTER H WITH CARON]
        case '\u0265' => 'h' // ɥ  [LATIN SMALL LETTER TURNED H]
        case '\u0266' => 'h' // ɦ  [LATIN SMALL LETTER H WITH HOOK]
        case '\u02AE' => 'h' // ʮ  [LATIN SMALL LETTER TURNED H WITH FISHHOOK]
        case '\u02AF' => 'h' // ʯ  [LATIN SMALL LETTER TURNED H WITH FISHHOOK AND TAIL]
        case '\u1E23' => 'h' // ḣ  [LATIN SMALL LETTER H WITH DOT ABOVE]
        case '\u1E25' => 'h' // ḥ  [LATIN SMALL LETTER H WITH DOT BELOW]
        case '\u1E27' => 'h' // ḧ  [LATIN SMALL LETTER H WITH DIAERESIS]
        case '\u1E29' => 'h' // ḩ  [LATIN SMALL LETTER H WITH CEDILLA]
        case '\u1E2B' => 'h' // ḫ  [LATIN SMALL LETTER H WITH BREVE BELOW]
        case '\u1E96' => 'h' // ẖ  [LATIN SMALL LETTER H WITH LINE BELOW]
        case '\u24D7' => 'h' // ⓗ  [CIRCLED LATIN SMALL LETTER H]
        case '\u2C68' => 'h' // ⱨ  [LATIN SMALL LETTER H WITH DESCENDER]
        case '\u2C76' => 'h' // ⱶ  [LATIN SMALL LETTER HALF H]
        case '\uFF48' => 'h' // ｈ  [FULLWIDTH LATIN SMALL LETTER H]

        case '\u00CC' => 'I' // Ì  [LATIN CAPITAL LETTER I WITH GRAVE]
        case '\u00CD' => 'I' // Í  [LATIN CAPITAL LETTER I WITH ACUTE]
        case '\u00CE' => 'I' // Î  [LATIN CAPITAL LETTER I WITH CIRCUMFLEX]
        case '\u00CF' => 'I' // Ï  [LATIN CAPITAL LETTER I WITH DIAERESIS]
        case '\u0128' => 'I' // Ĩ  [LATIN CAPITAL LETTER I WITH TILDE]
        case '\u012A' => 'I' // Ī  [LATIN CAPITAL LETTER I WITH MACRON]
        case '\u012C' => 'I' // Ĭ  [LATIN CAPITAL LETTER I WITH BREVE]
        case '\u012E' => 'I' // Į  [LATIN CAPITAL LETTER I WITH OGONEK]
        case '\u0130' => 'I' // İ  [LATIN CAPITAL LETTER I WITH DOT ABOVE]
        case '\u0196' => 'I' // Ɩ  [LATIN CAPITAL LETTER IOTA]
        case '\u0197' => 'I' // Ɨ  [LATIN CAPITAL LETTER I WITH STROKE]
        case '\u01CF' => 'I' // Ǐ  [LATIN CAPITAL LETTER I WITH CARON]
        case '\u0208' => 'I' // Ȉ  [LATIN CAPITAL LETTER I WITH DOUBLE GRAVE]
        case '\u020A' => 'I' // Ȋ  [LATIN CAPITAL LETTER I WITH INVERTED BREVE]
        case '\u026A' => 'I' // ɪ  [LATIN LETTER SMALL CAPITAL I]
        case '\u1D7B' => 'I' // ᵻ  [LATIN SMALL CAPITAL LETTER I WITH STROKE]
        case '\u1E2C' => 'I' // Ḭ  [LATIN CAPITAL LETTER I WITH TILDE BELOW]
        case '\u1E2E' => 'I' // Ḯ  [LATIN CAPITAL LETTER I WITH DIAERESIS AND ACUTE]
        case '\u1EC8' => 'I' // Ỉ  [LATIN CAPITAL LETTER I WITH HOOK ABOVE]
        case '\u1ECA' => 'I' // Ị  [LATIN CAPITAL LETTER I WITH DOT BELOW]
        case '\u24BE' => 'I' // Ⓘ  [CIRCLED LATIN CAPITAL LETTER I]
        case '\uA7FE' => 'I' // ꟾ  [LATIN EPIGRAPHIC LETTER I LONGA]
        case '\uFF29' => 'I' // Ｉ  [FULLWIDTH LATIN CAPITAL LETTER I]

        case '\u00EC' => 'i' // ì  [LATIN SMALL LETTER I WITH GRAVE]
        case '\u00ED' => 'i' // í  [LATIN SMALL LETTER I WITH ACUTE]
        case '\u00EE' => 'i' // î  [LATIN SMALL LETTER I WITH CIRCUMFLEX]
        case '\u00EF' => 'i' // ï  [LATIN SMALL LETTER I WITH DIAERESIS]
        case '\u0129' => 'i' // ĩ  [LATIN SMALL LETTER I WITH TILDE]
        case '\u012B' => 'i' // ī  [LATIN SMALL LETTER I WITH MACRON]
        case '\u012D' => 'i' // ĭ  [LATIN SMALL LETTER I WITH BREVE]
        case '\u012F' => 'i' // į  [LATIN SMALL LETTER I WITH OGONEK]
        case '\u0131' => 'i' // ı  [LATIN SMALL LETTER DOTLESS I]
        case '\u01D0' => 'i' // ǐ  [LATIN SMALL LETTER I WITH CARON]
        case '\u0209' => 'i' // ȉ  [LATIN SMALL LETTER I WITH DOUBLE GRAVE]
        case '\u020B' => 'i' // ȋ  [LATIN SMALL LETTER I WITH INVERTED BREVE]
        case '\u0268' => 'i' // ɨ  [LATIN SMALL LETTER I WITH STROKE]
        case '\u1D09' => 'i' // ᴉ  [LATIN SMALL LETTER TURNED I]
        case '\u1D62' => 'i' // ᵢ  [LATIN SUBSCRIPT SMALL LETTER I]
        case '\u1D7C' => 'i' // ᵼ  [LATIN SMALL LETTER IOTA WITH STROKE]
        case '\u1D96' => 'i' // ᶖ  [LATIN SMALL LETTER I WITH RETROFLEX HOOK]
        case '\u1E2D' => 'i' // ḭ  [LATIN SMALL LETTER I WITH TILDE BELOW]
        case '\u1E2F' => 'i' // ḯ  [LATIN SMALL LETTER I WITH DIAERESIS AND ACUTE]
        case '\u1EC9' => 'i' // ỉ  [LATIN SMALL LETTER I WITH HOOK ABOVE]
        case '\u1ECB' => 'i' // ị  [LATIN SMALL LETTER I WITH DOT BELOW]
        case '\u2071' => 'i' // ⁱ  [SUPERSCRIPT LATIN SMALL LETTER I]
        case '\u24D8' => 'i' // ⓘ  [CIRCLED LATIN SMALL LETTER I]
        case '\uFF49' => 'i' // ｉ  [FULLWIDTH LATIN SMALL LETTER I]

        case '\u0134' => 'J' // Ĵ  [LATIN CAPITAL LETTER J WITH CIRCUMFLEX]
        case '\u0248' => 'J' // Ɉ  [LATIN CAPITAL LETTER J WITH STROKE]
        case '\u1D0A' => 'J' // ᴊ  [LATIN LETTER SMALL CAPITAL J]
        case '\u24BF' => 'J' // Ⓙ  [CIRCLED LATIN CAPITAL LETTER J]
        case '\uFF2A' => 'J' // Ｊ  [FULLWIDTH LATIN CAPITAL LETTER J]

        case '\u0135' => 'j' // ĵ  [LATIN SMALL LETTER J WITH CIRCUMFLEX]
        case '\u01F0' => 'j' // ǰ  [LATIN SMALL LETTER J WITH CARON]
        case '\u0237' => 'j' // ȷ  [LATIN SMALL LETTER DOTLESS J]
        case '\u0249' => 'j' // ɉ  [LATIN SMALL LETTER J WITH STROKE]
        case '\u025F' => 'j' // ɟ  [LATIN SMALL LETTER DOTLESS J WITH STROKE]
        case '\u0284' => 'j' // ʄ  [LATIN SMALL LETTER DOTLESS J WITH STROKE AND HOOK]
        case '\u029D' => 'j' // ʝ  [LATIN SMALL LETTER J WITH CROSSED-TAIL]
        case '\u24D9' => 'j' // ⓙ  [CIRCLED LATIN SMALL LETTER J]
        case '\u2C7C' => 'j' // ⱼ  [LATIN SUBSCRIPT SMALL LETTER J]
        case '\uFF4A' => 'j' // ｊ  [FULLWIDTH LATIN SMALL LETTER J]

        case '\u0136' => 'K' // Ķ  [LATIN CAPITAL LETTER K WITH CEDILLA]
        case '\u0198' => 'K' // Ƙ  [LATIN CAPITAL LETTER K WITH HOOK]
        case '\u01E8' => 'K' // Ǩ  [LATIN CAPITAL LETTER K WITH CARON]
        case '\u1D0B' => 'K' // ᴋ  [LATIN LETTER SMALL CAPITAL K]
        case '\u1E30' => 'K' // Ḱ  [LATIN CAPITAL LETTER K WITH ACUTE]
        case '\u1E32' => 'K' // Ḳ  [LATIN CAPITAL LETTER K WITH DOT BELOW]
        case '\u1E34' => 'K' // Ḵ  [LATIN CAPITAL LETTER K WITH LINE BELOW]
        case '\u24C0' => 'K' // Ⓚ  [CIRCLED LATIN CAPITAL LETTER K]
        case '\u2C69' => 'K' // Ⱪ  [LATIN CAPITAL LETTER K WITH DESCENDER]
        case '\uA740' => 'K' // Ꝁ  [LATIN CAPITAL LETTER K WITH STROKE]
        case '\uA742' => 'K' // Ꝃ  [LATIN CAPITAL LETTER K WITH DIAGONAL STROKE]
        case '\uA744' => 'K' // Ꝅ  [LATIN CAPITAL LETTER K WITH STROKE AND DIAGONAL STROKE]
        case '\uFF2B' => 'K' // Ｋ  [FULLWIDTH LATIN CAPITAL LETTER K]

        case '\u0137' => 'k' // ķ  [LATIN SMALL LETTER K WITH CEDILLA]
        case '\u0199' => 'k' // ƙ  [LATIN SMALL LETTER K WITH HOOK]
        case '\u01E9' => 'k' // ǩ  [LATIN SMALL LETTER K WITH CARON]
        case '\u029E' => 'k' // ʞ  [LATIN SMALL LETTER TURNED K]
        case '\u1D84' => 'k' // ᶄ  [LATIN SMALL LETTER K WITH PALATAL HOOK]
        case '\u1E31' => 'k' // ḱ  [LATIN SMALL LETTER K WITH ACUTE]
        case '\u1E33' => 'k' // ḳ  [LATIN SMALL LETTER K WITH DOT BELOW]
        case '\u1E35' => 'k' // ḵ  [LATIN SMALL LETTER K WITH LINE BELOW]
        case '\u24DA' => 'k' // ⓚ  [CIRCLED LATIN SMALL LETTER K]
        case '\u2C6A' => 'k' // ⱪ  [LATIN SMALL LETTER K WITH DESCENDER]
        case '\uA741' => 'k' // ꝁ  [LATIN SMALL LETTER K WITH STROKE]
        case '\uA743' => 'k' // ꝃ  [LATIN SMALL LETTER K WITH DIAGONAL STROKE]
        case '\uA745' => 'k' // ꝅ  [LATIN SMALL LETTER K WITH STROKE AND DIAGONAL STROKE]
        case '\uFF4B' => 'k' // ｋ  [FULLWIDTH LATIN SMALL LETTER K]

        case '\u0139' => 'L' // Ĺ  [LATIN CAPITAL LETTER L WITH ACUTE]
        case '\u013B' => 'L' // Ļ  [LATIN CAPITAL LETTER L WITH CEDILLA]
        case '\u013D' => 'L' // Ľ  [LATIN CAPITAL LETTER L WITH CARON]
        case '\u013F' => 'L' // Ŀ  [LATIN CAPITAL LETTER L WITH MIDDLE DOT]
        case '\u0141' => 'L' // Ł  [LATIN CAPITAL LETTER L WITH STROKE]
        case '\u023D' => 'L' // Ƚ  [LATIN CAPITAL LETTER L WITH BAR]
        case '\u029F' => 'L' // ʟ  [LATIN LETTER SMALL CAPITAL L]
        case '\u1D0C' => 'L' // ᴌ  [LATIN LETTER SMALL CAPITAL L WITH STROKE]
        case '\u1E36' => 'L' // Ḷ  [LATIN CAPITAL LETTER L WITH DOT BELOW]
        case '\u1E38' => 'L' // Ḹ  [LATIN CAPITAL LETTER L WITH DOT BELOW AND MACRON]
        case '\u1E3A' => 'L' // Ḻ  [LATIN CAPITAL LETTER L WITH LINE BELOW]
        case '\u1E3C' => 'L' // Ḽ  [LATIN CAPITAL LETTER L WITH CIRCUMFLEX BELOW]
        case '\u24C1' => 'L' // Ⓛ  [CIRCLED LATIN CAPITAL LETTER L]
        case '\u2C60' => 'L' // Ⱡ  [LATIN CAPITAL LETTER L WITH DOUBLE BAR]
        case '\u2C62' => 'L' // Ɫ  [LATIN CAPITAL LETTER L WITH MIDDLE TILDE]
        case '\uA746' => 'L' // Ꝇ  [LATIN CAPITAL LETTER BROKEN L]
        case '\uA748' => 'L' // Ꝉ  [LATIN CAPITAL LETTER L WITH HIGH STROKE]
        case '\uA780' => 'L' // Ꞁ  [LATIN CAPITAL LETTER TURNED L]
        case '\uFF2C' => 'L' // Ｌ  [FULLWIDTH LATIN CAPITAL LETTER L]

        case '\u013A' => 'l' // ĺ  [LATIN SMALL LETTER L WITH ACUTE]
        case '\u013C' => 'l' // ļ  [LATIN SMALL LETTER L WITH CEDILLA]
        case '\u013E' => 'l' // ľ  [LATIN SMALL LETTER L WITH CARON]
        case '\u0140' => 'l' // ŀ  [LATIN SMALL LETTER L WITH MIDDLE DOT]
        case '\u0142' => 'l' // ł  [LATIN SMALL LETTER L WITH STROKE]
        case '\u019A' => 'l' // ƚ  [LATIN SMALL LETTER L WITH BAR]
        case '\u0234' => 'l' // ȴ  [LATIN SMALL LETTER L WITH CURL]
        case '\u026B' => 'l' // ɫ  [LATIN SMALL LETTER L WITH MIDDLE TILDE]
        case '\u026C' => 'l' // ɬ  [LATIN SMALL LETTER L WITH BELT]
        case '\u026D' => 'l' // ɭ  [LATIN SMALL LETTER L WITH RETROFLEX HOOK]
        case '\u1D85' => 'l' // ᶅ  [LATIN SMALL LETTER L WITH PALATAL HOOK]
        case '\u1E37' => 'l' // ḷ  [LATIN SMALL LETTER L WITH DOT BELOW]
        case '\u1E39' => 'l' // ḹ  [LATIN SMALL LETTER L WITH DOT BELOW AND MACRON]
        case '\u1E3B' => 'l' // ḻ  [LATIN SMALL LETTER L WITH LINE BELOW]
        case '\u1E3D' => 'l' // ḽ  [LATIN SMALL LETTER L WITH CIRCUMFLEX BELOW]
        case '\u24DB' => 'l' // ⓛ  [CIRCLED LATIN SMALL LETTER L]
        case '\u2C61' => 'l' // ⱡ  [LATIN SMALL LETTER L WITH DOUBLE BAR]
        case '\uA747' => 'l' // ꝇ  [LATIN SMALL LETTER BROKEN L]
        case '\uA749' => 'l' // ꝉ  [LATIN SMALL LETTER L WITH HIGH STROKE]
        case '\uA781' => 'l' // ꞁ  [LATIN SMALL LETTER TURNED L]
        case '\uFF4C' => 'l' // ｌ  [FULLWIDTH LATIN SMALL LETTER L]

        case '\u019C' => 'M' // Ɯ  [LATIN CAPITAL LETTER TURNED M]
        case '\u1D0D' => 'M' // ᴍ  [LATIN LETTER SMALL CAPITAL M]
        case '\u1E3E' => 'M' // Ḿ  [LATIN CAPITAL LETTER M WITH ACUTE]
        case '\u1E40' => 'M' // Ṁ  [LATIN CAPITAL LETTER M WITH DOT ABOVE]
        case '\u1E42' => 'M' // Ṃ  [LATIN CAPITAL LETTER M WITH DOT BELOW]
        case '\u24C2' => 'M' // Ⓜ  [CIRCLED LATIN CAPITAL LETTER M]
        case '\u2C6E' => 'M' // Ɱ  [LATIN CAPITAL LETTER M WITH HOOK]
        case '\uA7FD' => 'M' // ꟽ  [LATIN EPIGRAPHIC LETTER INVERTED M]
        case '\uA7FF' => 'M' // ꟿ  [LATIN EPIGRAPHIC LETTER ARCHAIC M]
        case '\uFF2D' => 'M' // Ｍ  [FULLWIDTH LATIN CAPITAL LETTER M]

        case '\u026F' => 'm' // ɯ  [LATIN SMALL LETTER TURNED M]
        case '\u0270' => 'm' // ɰ  [LATIN SMALL LETTER TURNED M WITH LONG LEG]
        case '\u0271' => 'm' // ɱ  [LATIN SMALL LETTER M WITH HOOK]
        case '\u1D6F' => 'm' // ᵯ  [LATIN SMALL LETTER M WITH MIDDLE TILDE]
        case '\u1D86' => 'm' // ᶆ  [LATIN SMALL LETTER M WITH PALATAL HOOK]
        case '\u1E3F' => 'm' // ḿ  [LATIN SMALL LETTER M WITH ACUTE]
        case '\u1E41' => 'm' // ṁ  [LATIN SMALL LETTER M WITH DOT ABOVE]
        case '\u1E43' => 'm' // ṃ  [LATIN SMALL LETTER M WITH DOT BELOW]
        case '\u24DC' => 'm' // ⓜ  [CIRCLED LATIN SMALL LETTER M]
        case '\uFF4D' => 'm' // ｍ  [FULLWIDTH LATIN SMALL LETTER M]

        case '\u00D1' => 'N' // Ñ  [LATIN CAPITAL LETTER N WITH TILDE]
        case '\u0143' => 'N' // Ń  [LATIN CAPITAL LETTER N WITH ACUTE]
        case '\u0145' => 'N' // Ņ  [LATIN CAPITAL LETTER N WITH CEDILLA]
        case '\u0147' => 'N' // Ň  [LATIN CAPITAL LETTER N WITH CARON]
        case '\u014A' => 'N' // Ŋ  http://en.wikipedia.org/wiki/Eng_(letter)  [LATIN CAPITAL LETTER ENG]
        case '\u019D' => 'N' // Ɲ  [LATIN CAPITAL LETTER N WITH LEFT HOOK]
        case '\u01F8' => 'N' // Ǹ  [LATIN CAPITAL LETTER N WITH GRAVE]
        case '\u0220' => 'N' // Ƞ  [LATIN CAPITAL LETTER N WITH LONG RIGHT LEG]
        case '\u0274' => 'N' // ɴ  [LATIN LETTER SMALL CAPITAL N]
        case '\u1D0E' => 'N' // ᴎ  [LATIN LETTER SMALL CAPITAL REVERSED N]
        case '\u1E44' => 'N' // Ṅ  [LATIN CAPITAL LETTER N WITH DOT ABOVE]
        case '\u1E46' => 'N' // Ṇ  [LATIN CAPITAL LETTER N WITH DOT BELOW]
        case '\u1E48' => 'N' // Ṉ  [LATIN CAPITAL LETTER N WITH LINE BELOW]
        case '\u1E4A' => 'N' // Ṋ  [LATIN CAPITAL LETTER N WITH CIRCUMFLEX BELOW]
        case '\u24C3' => 'N' // Ⓝ  [CIRCLED LATIN CAPITAL LETTER N]
        case '\uFF2E' => 'N' // Ｎ  [FULLWIDTH LATIN CAPITAL LETTER N]

        case '\u00F1' => 'n' // ñ  [LATIN SMALL LETTER N WITH TILDE]
        case '\u0144' => 'n' // ń  [LATIN SMALL LETTER N WITH ACUTE]
        case '\u0146' => 'n' // ņ  [LATIN SMALL LETTER N WITH CEDILLA]
        case '\u0148' => 'n' // ň  [LATIN SMALL LETTER N WITH CARON]
        case '\u0149' => 'n' // ŉ  [LATIN SMALL LETTER N PRECEDED BY APOSTROPHE]
        case '\u014B' => 'n' // ŋ  http://en.wikipedia.org/wiki/Eng_(letter)  [LATIN SMALL LETTER ENG]
        case '\u019E' => 'n' // ƞ  [LATIN SMALL LETTER N WITH LONG RIGHT LEG]
        case '\u01F9' => 'n' // ǹ  [LATIN SMALL LETTER N WITH GRAVE]
        case '\u0235' => 'n' // ȵ  [LATIN SMALL LETTER N WITH CURL]
        case '\u0272' => 'n' // ɲ  [LATIN SMALL LETTER N WITH LEFT HOOK]
        case '\u0273' => 'n' // ɳ  [LATIN SMALL LETTER N WITH RETROFLEX HOOK]
        case '\u1D70' => 'n' // ᵰ  [LATIN SMALL LETTER N WITH MIDDLE TILDE]
        case '\u1D87' => 'n' // ᶇ  [LATIN SMALL LETTER N WITH PALATAL HOOK]
        case '\u1E45' => 'n' // ṅ  [LATIN SMALL LETTER N WITH DOT ABOVE]
        case '\u1E47' => 'n' // ṇ  [LATIN SMALL LETTER N WITH DOT BELOW]
        case '\u1E49' => 'n' // ṉ  [LATIN SMALL LETTER N WITH LINE BELOW]
        case '\u1E4B' => 'n' // ṋ  [LATIN SMALL LETTER N WITH CIRCUMFLEX BELOW]
        case '\u207F' => 'n' // ⁿ  [SUPERSCRIPT LATIN SMALL LETTER N]
        case '\u24DD' => 'n' // ⓝ  [CIRCLED LATIN SMALL LETTER N]
        case '\uFF4E' => 'n' // ｎ  [FULLWIDTH LATIN SMALL LETTER N]

        case '\u00D2' => 'O' // Ò  [LATIN CAPITAL LETTER O WITH GRAVE]
        case '\u00D3' => 'O' // Ó  [LATIN CAPITAL LETTER O WITH ACUTE]
        case '\u00D4' => 'O' // Ô  [LATIN CAPITAL LETTER O WITH CIRCUMFLEX]
        case '\u00D5' => 'O' // Õ  [LATIN CAPITAL LETTER O WITH TILDE]
        case '\u00D6' => 'O' // Ö  [LATIN CAPITAL LETTER O WITH DIAERESIS]
        case '\u00D8' => 'O' // Ø  [LATIN CAPITAL LETTER O WITH STROKE]
        case '\u014C' => 'O' // Ō  [LATIN CAPITAL LETTER O WITH MACRON]
        case '\u014E' => 'O' // Ŏ  [LATIN CAPITAL LETTER O WITH BREVE]
        case '\u0150' => 'O' // Ő  [LATIN CAPITAL LETTER O WITH DOUBLE ACUTE]
        case '\u0186' => 'O' // Ɔ  [LATIN CAPITAL LETTER OPEN O]
        case '\u019F' => 'O' // Ɵ  [LATIN CAPITAL LETTER O WITH MIDDLE TILDE]
        case '\u01A0' => 'O' // Ơ  [LATIN CAPITAL LETTER O WITH HORN]
        case '\u01D1' => 'O' // Ǒ  [LATIN CAPITAL LETTER O WITH CARON]
        case '\u01EA' => 'O' // Ǫ  [LATIN CAPITAL LETTER O WITH OGONEK]
        case '\u01EC' => 'O' // Ǭ  [LATIN CAPITAL LETTER O WITH OGONEK AND MACRON]
        case '\u01FE' => 'O' // Ǿ  [LATIN CAPITAL LETTER O WITH STROKE AND ACUTE]
        case '\u020C' => 'O' // Ȍ  [LATIN CAPITAL LETTER O WITH DOUBLE GRAVE]
        case '\u020E' => 'O' // Ȏ  [LATIN CAPITAL LETTER O WITH INVERTED BREVE]
        case '\u022A' => 'O' // Ȫ  [LATIN CAPITAL LETTER O WITH DIAERESIS AND MACRON]
        case '\u022C' => 'O' // Ȭ  [LATIN CAPITAL LETTER O WITH TILDE AND MACRON]
        case '\u022E' => 'O' // Ȯ  [LATIN CAPITAL LETTER O WITH DOT ABOVE]
        case '\u0230' => 'O' // Ȱ  [LATIN CAPITAL LETTER O WITH DOT ABOVE AND MACRON]
        case '\u1D0F' => 'O' // ᴏ  [LATIN LETTER SMALL CAPITAL O]
        case '\u1D10' => 'O' // ᴐ  [LATIN LETTER SMALL CAPITAL OPEN O]
        case '\u1E4C' => 'O' // Ṍ  [LATIN CAPITAL LETTER O WITH TILDE AND ACUTE]
        case '\u1E4E' => 'O' // Ṏ  [LATIN CAPITAL LETTER O WITH TILDE AND DIAERESIS]
        case '\u1E50' => 'O' // Ṑ  [LATIN CAPITAL LETTER O WITH MACRON AND GRAVE]
        case '\u1E52' => 'O' // Ṓ  [LATIN CAPITAL LETTER O WITH MACRON AND ACUTE]
        case '\u1ECC' => 'O' // Ọ  [LATIN CAPITAL LETTER O WITH DOT BELOW]
        case '\u1ECE' => 'O' // Ỏ  [LATIN CAPITAL LETTER O WITH HOOK ABOVE]
        case '\u1ED0' => 'O' // Ố  [LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND ACUTE]
        case '\u1ED2' => 'O' // Ồ  [LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND GRAVE]
        case '\u1ED4' => 'O' // Ổ  [LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND HOOK ABOVE]
        case '\u1ED6' => 'O' // Ỗ  [LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND TILDE]
        case '\u1ED8' => 'O' // Ộ  [LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND DOT BELOW]
        case '\u1EDA' => 'O' // Ớ  [LATIN CAPITAL LETTER O WITH HORN AND ACUTE]
        case '\u1EDC' => 'O' // Ờ  [LATIN CAPITAL LETTER O WITH HORN AND GRAVE]
        case '\u1EDE' => 'O' // Ở  [LATIN CAPITAL LETTER O WITH HORN AND HOOK ABOVE]
        case '\u1EE0' => 'O' // Ỡ  [LATIN CAPITAL LETTER O WITH HORN AND TILDE]
        case '\u1EE2' => 'O' // Ợ  [LATIN CAPITAL LETTER O WITH HORN AND DOT BELOW]
        case '\u24C4' => 'O' // Ⓞ  [CIRCLED LATIN CAPITAL LETTER O]
        case '\uA74A' => 'O' // Ꝋ  [LATIN CAPITAL LETTER O WITH LONG STROKE OVERLAY]
        case '\uA74C' => 'O' // Ꝍ  [LATIN CAPITAL LETTER O WITH LOOP]
        case '\uFF2F' => 'O' // Ｏ  [FULLWIDTH LATIN CAPITAL LETTER O]

        case '\u00F2' => 'o' // ò  [LATIN SMALL LETTER O WITH GRAVE]
        case '\u00F3' => 'o' // ó  [LATIN SMALL LETTER O WITH ACUTE]
        case '\u00F4' => 'o' // ô  [LATIN SMALL LETTER O WITH CIRCUMFLEX]
        case '\u00F5' => 'o' // õ  [LATIN SMALL LETTER O WITH TILDE]
        case '\u00F6' => 'o' // ö  [LATIN SMALL LETTER O WITH DIAERESIS]
        case '\u00F8' => 'o' // ø  [LATIN SMALL LETTER O WITH STROKE]
        case '\u014D' => 'o' // ō  [LATIN SMALL LETTER O WITH MACRON]
        case '\u014F' => 'o' // ŏ  [LATIN SMALL LETTER O WITH BREVE]
        case '\u0151' => 'o' // ő  [LATIN SMALL LETTER O WITH DOUBLE ACUTE]
        case '\u01A1' => 'o' // ơ  [LATIN SMALL LETTER O WITH HORN]
        case '\u01D2' => 'o' // ǒ  [LATIN SMALL LETTER O WITH CARON]
        case '\u01EB' => 'o' // ǫ  [LATIN SMALL LETTER O WITH OGONEK]
        case '\u01ED' => 'o' // ǭ  [LATIN SMALL LETTER O WITH OGONEK AND MACRON]
        case '\u01FF' => 'o' // ǿ  [LATIN SMALL LETTER O WITH STROKE AND ACUTE]
        case '\u020D' => 'o' // ȍ  [LATIN SMALL LETTER O WITH DOUBLE GRAVE]
        case '\u020F' => 'o' // ȏ  [LATIN SMALL LETTER O WITH INVERTED BREVE]
        case '\u022B' => 'o' // ȫ  [LATIN SMALL LETTER O WITH DIAERESIS AND MACRON]
        case '\u022D' => 'o' // ȭ  [LATIN SMALL LETTER O WITH TILDE AND MACRON]
        case '\u022F' => 'o' // ȯ  [LATIN SMALL LETTER O WITH DOT ABOVE]
        case '\u0231' => 'o' // ȱ  [LATIN SMALL LETTER O WITH DOT ABOVE AND MACRON]
        case '\u0254' => 'o' // ɔ  [LATIN SMALL LETTER OPEN O]
        case '\u0275' => 'o' // ɵ  [LATIN SMALL LETTER BARRED O]
        case '\u1D16' => 'o' // ᴖ  [LATIN SMALL LETTER TOP HALF O]
        case '\u1D17' => 'o' // ᴗ  [LATIN SMALL LETTER BOTTOM HALF O]
        case '\u1D97' => 'o' // ᶗ  [LATIN SMALL LETTER OPEN O WITH RETROFLEX HOOK]
        case '\u1E4D' => 'o' // ṍ  [LATIN SMALL LETTER O WITH TILDE AND ACUTE]
        case '\u1E4F' => 'o' // ṏ  [LATIN SMALL LETTER O WITH TILDE AND DIAERESIS]
        case '\u1E51' => 'o' // ṑ  [LATIN SMALL LETTER O WITH MACRON AND GRAVE]
        case '\u1E53' => 'o' // ṓ  [LATIN SMALL LETTER O WITH MACRON AND ACUTE]
        case '\u1ECD' => 'o' // ọ  [LATIN SMALL LETTER O WITH DOT BELOW]
        case '\u1ECF' => 'o' // ỏ  [LATIN SMALL LETTER O WITH HOOK ABOVE]
        case '\u1ED1' => 'o' // ố  [LATIN SMALL LETTER O WITH CIRCUMFLEX AND ACUTE]
        case '\u1ED3' => 'o' // ồ  [LATIN SMALL LETTER O WITH CIRCUMFLEX AND GRAVE]
        case '\u1ED5' => 'o' // ổ  [LATIN SMALL LETTER O WITH CIRCUMFLEX AND HOOK ABOVE]
        case '\u1ED7' => 'o' // ỗ  [LATIN SMALL LETTER O WITH CIRCUMFLEX AND TILDE]
        case '\u1ED9' => 'o' // ộ  [LATIN SMALL LETTER O WITH CIRCUMFLEX AND DOT BELOW]
        case '\u1EDB' => 'o' // ớ  [LATIN SMALL LETTER O WITH HORN AND ACUTE]
        case '\u1EDD' => 'o' // ờ  [LATIN SMALL LETTER O WITH HORN AND GRAVE]
        case '\u1EDF' => 'o' // ở  [LATIN SMALL LETTER O WITH HORN AND HOOK ABOVE]
        case '\u1EE1' => 'o' // ỡ  [LATIN SMALL LETTER O WITH HORN AND TILDE]
        case '\u1EE3' => 'o' // ợ  [LATIN SMALL LETTER O WITH HORN AND DOT BELOW]
        case '\u2092' => 'o' // ₒ  [LATIN SUBSCRIPT SMALL LETTER O]
        case '\u24DE' => 'o' // ⓞ  [CIRCLED LATIN SMALL LETTER O]
        case '\u2C7A' => 'o' // ⱺ  [LATIN SMALL LETTER O WITH LOW RING INSIDE]
        case '\uA74B' => 'o' // ꝋ  [LATIN SMALL LETTER O WITH LONG STROKE OVERLAY]
        case '\uA74D' => 'o' // ꝍ  [LATIN SMALL LETTER O WITH LOOP]
        case '\uFF4F' => 'o' // ｏ  [FULLWIDTH LATIN SMALL LETTER O]

        case '\u01A4' => 'P' // Ƥ  [LATIN CAPITAL LETTER P WITH HOOK]
        case '\u1D18' => 'P' // ᴘ  [LATIN LETTER SMALL CAPITAL P]
        case '\u1E54' => 'P' // Ṕ  [LATIN CAPITAL LETTER P WITH ACUTE]
        case '\u1E56' => 'P' // Ṗ  [LATIN CAPITAL LETTER P WITH DOT ABOVE]
        case '\u24C5' => 'P' // Ⓟ  [CIRCLED LATIN CAPITAL LETTER P]
        case '\u2C63' => 'P' // Ᵽ  [LATIN CAPITAL LETTER P WITH STROKE]
        case '\uA750' => 'P' // Ꝑ  [LATIN CAPITAL LETTER P WITH STROKE THROUGH DESCENDER]
        case '\uA752' => 'P' // Ꝓ  [LATIN CAPITAL LETTER P WITH FLOURISH]
        case '\uA754' => 'P' // Ꝕ  [LATIN CAPITAL LETTER P WITH SQUIRREL TAIL]
        case '\uFF30' => 'P' // Ｐ  [FULLWIDTH LATIN CAPITAL LETTER P]

        case '\u01A5' => 'p' // ƥ  [LATIN SMALL LETTER P WITH HOOK]
        case '\u1D71' => 'p' // ᵱ  [LATIN SMALL LETTER P WITH MIDDLE TILDE]
        case '\u1D7D' => 'p' // ᵽ  [LATIN SMALL LETTER P WITH STROKE]
        case '\u1D88' => 'p' // ᶈ  [LATIN SMALL LETTER P WITH PALATAL HOOK]
        case '\u1E55' => 'p' // ṕ  [LATIN SMALL LETTER P WITH ACUTE]
        case '\u1E57' => 'p' // ṗ  [LATIN SMALL LETTER P WITH DOT ABOVE]
        case '\u24DF' => 'p' // ⓟ  [CIRCLED LATIN SMALL LETTER P]
        case '\uA751' => 'p' // ꝑ  [LATIN SMALL LETTER P WITH STROKE THROUGH DESCENDER]
        case '\uA753' => 'p' // ꝓ  [LATIN SMALL LETTER P WITH FLOURISH]
        case '\uA755' => 'p' // ꝕ  [LATIN SMALL LETTER P WITH SQUIRREL TAIL]
        case '\uA7FC' => 'p' // ꟼ  [LATIN EPIGRAPHIC LETTER REVERSED P]
        case '\uFF50' => 'p' // ｐ  [FULLWIDTH LATIN SMALL LETTER P]

//          case '\u24AB' => "(p)" // ⒫  [PARENTHESIZED LATIN SMALL LETTER P]

        case '\u024A' => 'Q' // Ɋ  [LATIN CAPITAL LETTER SMALL Q WITH HOOK TAIL]
        case '\u24C6' => 'Q' // Ⓠ  [CIRCLED LATIN CAPITAL LETTER Q]
        case '\uA756' => 'Q' // Ꝗ  [LATIN CAPITAL LETTER Q WITH STROKE THROUGH DESCENDER]
        case '\uA758' => 'Q' // Ꝙ  [LATIN CAPITAL LETTER Q WITH DIAGONAL STROKE]
        case '\uFF31' => 'Q' // Ｑ  [FULLWIDTH LATIN CAPITAL LETTER Q]

        case '\u0138' => 'q' // ĸ  http://en.wikipedia.org/wiki/Kra_(letter)  [LATIN SMALL LETTER KRA]
        case '\u024B' => 'q' // ɋ  [LATIN SMALL LETTER Q WITH HOOK TAIL]
        case '\u02A0' => 'q' // ʠ  [LATIN SMALL LETTER Q WITH HOOK]
        case '\u24E0' => 'q' // ⓠ  [CIRCLED LATIN SMALL LETTER Q]
        case '\uA757' => 'q' // ꝗ  [LATIN SMALL LETTER Q WITH STROKE THROUGH DESCENDER]
        case '\uA759' => 'q' // ꝙ  [LATIN SMALL LETTER Q WITH DIAGONAL STROKE]
        case '\uFF51' => 'q' // ｑ  [FULLWIDTH LATIN SMALL LETTER Q]

        case '\u0154' => 'R' // Ŕ  [LATIN CAPITAL LETTER R WITH ACUTE]
        case '\u0156' => 'R' // Ŗ  [LATIN CAPITAL LETTER R WITH CEDILLA]
        case '\u0158' => 'R' // Ř  [LATIN CAPITAL LETTER R WITH CARON]
        case '\u0210' => 'R' // Ȓ  [LATIN CAPITAL LETTER R WITH DOUBLE GRAVE]
        case '\u0212' => 'R' // Ȓ  [LATIN CAPITAL LETTER R WITH INVERTED BREVE]
        case '\u024C' => 'R' // Ɍ  [LATIN CAPITAL LETTER R WITH STROKE]
        case '\u0280' => 'R' // ʀ  [LATIN LETTER SMALL CAPITAL R]
        case '\u0281' => 'R' // ʁ  [LATIN LETTER SMALL CAPITAL INVERTED R]
        case '\u1D19' => 'R' // ᴙ  [LATIN LETTER SMALL CAPITAL REVERSED R]
        case '\u1D1A' => 'R' // ᴚ  [LATIN LETTER SMALL CAPITAL TURNED R]
        case '\u1E58' => 'R' // Ṙ  [LATIN CAPITAL LETTER R WITH DOT ABOVE]
        case '\u1E5A' => 'R' // Ṛ  [LATIN CAPITAL LETTER R WITH DOT BELOW]
        case '\u1E5C' => 'R' // Ṝ  [LATIN CAPITAL LETTER R WITH DOT BELOW AND MACRON]
        case '\u1E5E' => 'R' // Ṟ  [LATIN CAPITAL LETTER R WITH LINE BELOW]
        case '\u24C7' => 'R' // Ⓡ  [CIRCLED LATIN CAPITAL LETTER R]
        case '\u2C64' => 'R' // Ɽ  [LATIN CAPITAL LETTER R WITH TAIL]
        case '\uA75A' => 'R' // Ꝛ  [LATIN CAPITAL LETTER R ROTUNDA]
        case '\uA782' => 'R' // Ꞃ  [LATIN CAPITAL LETTER INSULAR R]
        case '\uFF32' => 'R' // Ｒ  [FULLWIDTH LATIN CAPITAL LETTER R]

        case '\u0155' => 'r' // ŕ  [LATIN SMALL LETTER R WITH ACUTE]
        case '\u0157' => 'r' // ŗ  [LATIN SMALL LETTER R WITH CEDILLA]
        case '\u0159' => 'r' // ř  [LATIN SMALL LETTER R WITH CARON]
        case '\u0211' => 'r' // ȑ  [LATIN SMALL LETTER R WITH DOUBLE GRAVE]
        case '\u0213' => 'r' // ȓ  [LATIN SMALL LETTER R WITH INVERTED BREVE]
        case '\u024D' => 'r' // ɍ  [LATIN SMALL LETTER R WITH STROKE]
        case '\u027C' => 'r' // ɼ  [LATIN SMALL LETTER R WITH LONG LEG]
        case '\u027D' => 'r' // ɽ  [LATIN SMALL LETTER R WITH TAIL]
        case '\u027E' => 'r' // ɾ  [LATIN SMALL LETTER R WITH FISHHOOK]
        case '\u027F' => 'r' // ɿ  [LATIN SMALL LETTER REVERSED R WITH FISHHOOK]
        case '\u1D63' => 'r' // ᵣ  [LATIN SUBSCRIPT SMALL LETTER R]
        case '\u1D72' => 'r' // ᵲ  [LATIN SMALL LETTER R WITH MIDDLE TILDE]
        case '\u1D73' => 'r' // ᵳ  [LATIN SMALL LETTER R WITH FISHHOOK AND MIDDLE TILDE]
        case '\u1D89' => 'r' // ᶉ  [LATIN SMALL LETTER R WITH PALATAL HOOK]
        case '\u1E59' => 'r' // ṙ  [LATIN SMALL LETTER R WITH DOT ABOVE]
        case '\u1E5B' => 'r' // ṛ  [LATIN SMALL LETTER R WITH DOT BELOW]
        case '\u1E5D' => 'r' // ṝ  [LATIN SMALL LETTER R WITH DOT BELOW AND MACRON]
        case '\u1E5F' => 'r' // ṟ  [LATIN SMALL LETTER R WITH LINE BELOW]
        case '\u24E1' => 'r' // ⓡ  [CIRCLED LATIN SMALL LETTER R]
        case '\uA75B' => 'r' // ꝛ  [LATIN SMALL LETTER R ROTUNDA]
        case '\uA783' => 'r' // ꞃ  [LATIN SMALL LETTER INSULAR R]
        case '\uFF52' => 'r' // ｒ  [FULLWIDTH LATIN SMALL LETTER R]

        case '\u015A' => 'S' // Ś  [LATIN CAPITAL LETTER S WITH ACUTE]
        case '\u015C' => 'S' // Ŝ  [LATIN CAPITAL LETTER S WITH CIRCUMFLEX]
        case '\u015E' => 'S' // Ş  [LATIN CAPITAL LETTER S WITH CEDILLA]
        case '\u0160' => 'S' // Š  [LATIN CAPITAL LETTER S WITH CARON]
        case '\u0218' => 'S' // Ș  [LATIN CAPITAL LETTER S WITH COMMA BELOW]
        case '\u1E60' => 'S' // Ṡ  [LATIN CAPITAL LETTER S WITH DOT ABOVE]
        case '\u1E62' => 'S' // Ṣ  [LATIN CAPITAL LETTER S WITH DOT BELOW]
        case '\u1E64' => 'S' // Ṥ  [LATIN CAPITAL LETTER S WITH ACUTE AND DOT ABOVE]
        case '\u1E66' => 'S' // Ṧ  [LATIN CAPITAL LETTER S WITH CARON AND DOT ABOVE]
        case '\u1E68' => 'S' // Ṩ  [LATIN CAPITAL LETTER S WITH DOT BELOW AND DOT ABOVE]
        case '\u24C8' => 'S' // Ⓢ  [CIRCLED LATIN CAPITAL LETTER S]
        case '\uA731' => 'S' // ꜱ  [LATIN LETTER SMALL CAPITAL S]
        case '\uA785' => 'S' // ꞅ  [LATIN SMALL LETTER INSULAR S]
        case '\uFF33' => 'S' // Ｓ  [FULLWIDTH LATIN CAPITAL LETTER S]

        case '\u015B' => 's' // ś  [LATIN SMALL LETTER S WITH ACUTE]
        case '\u015D' => 's' // ŝ  [LATIN SMALL LETTER S WITH CIRCUMFLEX]
        case '\u015F' => 's' // ş  [LATIN SMALL LETTER S WITH CEDILLA]
        case '\u0161' => 's' // š  [LATIN SMALL LETTER S WITH CARON]
        case '\u017F' => 's' // ſ  http://en.wikipedia.org/wiki/Long_S  [LATIN SMALL LETTER LONG S]
        case '\u0219' => 's' // ș  [LATIN SMALL LETTER S WITH COMMA BELOW]
        case '\u023F' => 's' // ȿ  [LATIN SMALL LETTER S WITH SWASH TAIL]
        case '\u0282' => 's' // ʂ  [LATIN SMALL LETTER S WITH HOOK]
        case '\u1D74' => 's' // ᵴ  [LATIN SMALL LETTER S WITH MIDDLE TILDE]
        case '\u1D8A' => 's' // ᶊ  [LATIN SMALL LETTER S WITH PALATAL HOOK]
        case '\u1E61' => 's' // ṡ  [LATIN SMALL LETTER S WITH DOT ABOVE]
        case '\u1E63' => 's' // ṣ  [LATIN SMALL LETTER S WITH DOT BELOW]
        case '\u1E65' => 's' // ṥ  [LATIN SMALL LETTER S WITH ACUTE AND DOT ABOVE]
        case '\u1E67' => 's' // ṧ  [LATIN SMALL LETTER S WITH CARON AND DOT ABOVE]
        case '\u1E69' => 's' // ṩ  [LATIN SMALL LETTER S WITH DOT BELOW AND DOT ABOVE]
        case '\u1E9C' => 's' // ẜ  [LATIN SMALL LETTER LONG S WITH DIAGONAL STROKE]
        case '\u1E9D' => 's' // ẝ  [LATIN SMALL LETTER LONG S WITH HIGH STROKE]
        case '\u24E2' => 's' // ⓢ  [CIRCLED LATIN SMALL LETTER S]
        case '\uA784' => 's' // Ꞅ  [LATIN CAPITAL LETTER INSULAR S]
        case '\uFF53' => 's' // ｓ  [FULLWIDTH LATIN SMALL LETTER S]

        case '\u0162' => 'T' // Ţ  [LATIN CAPITAL LETTER T WITH CEDILLA]
        case '\u0164' => 'T' // Ť  [LATIN CAPITAL LETTER T WITH CARON]
        case '\u0166' => 'T' // Ŧ  [LATIN CAPITAL LETTER T WITH STROKE]
        case '\u01AC' => 'T' // Ƭ  [LATIN CAPITAL LETTER T WITH HOOK]
        case '\u01AE' => 'T' // Ʈ  [LATIN CAPITAL LETTER T WITH RETROFLEX HOOK]
        case '\u021A' => 'T' // Ț  [LATIN CAPITAL LETTER T WITH COMMA BELOW]
        case '\u023E' => 'T' // Ⱦ  [LATIN CAPITAL LETTER T WITH DIAGONAL STROKE]
        case '\u1D1B' => 'T' // ᴛ  [LATIN LETTER SMALL CAPITAL T]
        case '\u1E6A' => 'T' // Ṫ  [LATIN CAPITAL LETTER T WITH DOT ABOVE]
        case '\u1E6C' => 'T' // Ṭ  [LATIN CAPITAL LETTER T WITH DOT BELOW]
        case '\u1E6E' => 'T' // Ṯ  [LATIN CAPITAL LETTER T WITH LINE BELOW]
        case '\u1E70' => 'T' // Ṱ  [LATIN CAPITAL LETTER T WITH CIRCUMFLEX BELOW]
        case '\u24C9' => 'T' // Ⓣ  [CIRCLED LATIN CAPITAL LETTER T]
        case '\uA786' => 'T' // Ꞇ  [LATIN CAPITAL LETTER INSULAR T]
        case '\uFF34' => 'T' // Ｔ  [FULLWIDTH LATIN CAPITAL LETTER T]

        case '\u0163' => 't' // ţ  [LATIN SMALL LETTER T WITH CEDILLA]
        case '\u0165' => 't' // ť  [LATIN SMALL LETTER T WITH CARON]
        case '\u0167' => 't' // ŧ  [LATIN SMALL LETTER T WITH STROKE]
        case '\u01AB' => 't' // ƫ  [LATIN SMALL LETTER T WITH PALATAL HOOK]
        case '\u01AD' => 't' // ƭ  [LATIN SMALL LETTER T WITH HOOK]
        case '\u021B' => 't' // ț  [LATIN SMALL LETTER T WITH COMMA BELOW]
        case '\u0236' => 't' // ȶ  [LATIN SMALL LETTER T WITH CURL]
        case '\u0287' => 't' // ʇ  [LATIN SMALL LETTER TURNED T]
        case '\u0288' => 't' // ʈ  [LATIN SMALL LETTER T WITH RETROFLEX HOOK]
        case '\u1D75' => 't' // ᵵ  [LATIN SMALL LETTER T WITH MIDDLE TILDE]
        case '\u1E6B' => 't' // ṫ  [LATIN SMALL LETTER T WITH DOT ABOVE]
        case '\u1E6D' => 't' // ṭ  [LATIN SMALL LETTER T WITH DOT BELOW]
        case '\u1E6F' => 't' // ṯ  [LATIN SMALL LETTER T WITH LINE BELOW]
        case '\u1E71' => 't' // ṱ  [LATIN SMALL LETTER T WITH CIRCUMFLEX BELOW]
        case '\u1E97' => 't' // ẗ  [LATIN SMALL LETTER T WITH DIAERESIS]
        case '\u24E3' => 't' // ⓣ  [CIRCLED LATIN SMALL LETTER T]
        case '\u2C66' => 't' // ⱦ  [LATIN SMALL LETTER T WITH DIAGONAL STROKE]
        case '\uFF54' => 't' // ｔ  [FULLWIDTH LATIN SMALL LETTER T]

        case '\u00D9' => 'U' // Ù  [LATIN CAPITAL LETTER U WITH GRAVE]
        case '\u00DA' => 'U' // Ú  [LATIN CAPITAL LETTER U WITH ACUTE]
        case '\u00DB' => 'U' // Û  [LATIN CAPITAL LETTER U WITH CIRCUMFLEX]
        case '\u00DC' => 'U' // Ü  [LATIN CAPITAL LETTER U WITH DIAERESIS]
        case '\u0168' => 'U' // Ũ  [LATIN CAPITAL LETTER U WITH TILDE]
        case '\u016A' => 'U' // Ū  [LATIN CAPITAL LETTER U WITH MACRON]
        case '\u016C' => 'U' // Ŭ  [LATIN CAPITAL LETTER U WITH BREVE]
        case '\u016E' => 'U' // Ů  [LATIN CAPITAL LETTER U WITH RING ABOVE]
        case '\u0170' => 'U' // Ű  [LATIN CAPITAL LETTER U WITH DOUBLE ACUTE]
        case '\u0172' => 'U' // Ų  [LATIN CAPITAL LETTER U WITH OGONEK]
        case '\u01AF' => 'U' // Ư  [LATIN CAPITAL LETTER U WITH HORN]
        case '\u01D3' => 'U' // Ǔ  [LATIN CAPITAL LETTER U WITH CARON]
        case '\u01D5' => 'U' // Ǖ  [LATIN CAPITAL LETTER U WITH DIAERESIS AND MACRON]
        case '\u01D7' => 'U' // Ǘ  [LATIN CAPITAL LETTER U WITH DIAERESIS AND ACUTE]
        case '\u01D9' => 'U' // Ǚ  [LATIN CAPITAL LETTER U WITH DIAERESIS AND CARON]
        case '\u01DB' => 'U' // Ǜ  [LATIN CAPITAL LETTER U WITH DIAERESIS AND GRAVE]
        case '\u0214' => 'U' // Ȕ  [LATIN CAPITAL LETTER U WITH DOUBLE GRAVE]
        case '\u0216' => 'U' // Ȗ  [LATIN CAPITAL LETTER U WITH INVERTED BREVE]
        case '\u0244' => 'U' // Ʉ  [LATIN CAPITAL LETTER U BAR]
        case '\u1D1C' => 'U' // ᴜ  [LATIN LETTER SMALL CAPITAL U]
        case '\u1D7E' => 'U' // ᵾ  [LATIN SMALL CAPITAL LETTER U WITH STROKE]
        case '\u1E72' => 'U' // Ṳ  [LATIN CAPITAL LETTER U WITH DIAERESIS BELOW]
        case '\u1E74' => 'U' // Ṵ  [LATIN CAPITAL LETTER U WITH TILDE BELOW]
        case '\u1E76' => 'U' // Ṷ  [LATIN CAPITAL LETTER U WITH CIRCUMFLEX BELOW]
        case '\u1E78' => 'U' // Ṹ  [LATIN CAPITAL LETTER U WITH TILDE AND ACUTE]
        case '\u1E7A' => 'U' // Ṻ  [LATIN CAPITAL LETTER U WITH MACRON AND DIAERESIS]
        case '\u1EE4' => 'U' // Ụ  [LATIN CAPITAL LETTER U WITH DOT BELOW]
        case '\u1EE6' => 'U' // Ủ  [LATIN CAPITAL LETTER U WITH HOOK ABOVE]
        case '\u1EE8' => 'U' // Ứ  [LATIN CAPITAL LETTER U WITH HORN AND ACUTE]
        case '\u1EEA' => 'U' // Ừ  [LATIN CAPITAL LETTER U WITH HORN AND GRAVE]
        case '\u1EEC' => 'U' // Ử  [LATIN CAPITAL LETTER U WITH HORN AND HOOK ABOVE]
        case '\u1EEE' => 'U' // Ữ  [LATIN CAPITAL LETTER U WITH HORN AND TILDE]
        case '\u1EF0' => 'U' // Ự  [LATIN CAPITAL LETTER U WITH HORN AND DOT BELOW]
        case '\u24CA' => 'U' // Ⓤ  [CIRCLED LATIN CAPITAL LETTER U]
        case '\uFF35' => 'U' // Ｕ  [FULLWIDTH LATIN CAPITAL LETTER U]

        case '\u00F9' => 'u' // ù  [LATIN SMALL LETTER U WITH GRAVE]
        case '\u00FA' => 'u' // ú  [LATIN SMALL LETTER U WITH ACUTE]
        case '\u00FB' => 'u' // û  [LATIN SMALL LETTER U WITH CIRCUMFLEX]
        case '\u00FC' => 'u' // ü  [LATIN SMALL LETTER U WITH DIAERESIS]
        case '\u0169' => 'u' // ũ  [LATIN SMALL LETTER U WITH TILDE]
        case '\u016B' => 'u' // ū  [LATIN SMALL LETTER U WITH MACRON]
        case '\u016D' => 'u' // ŭ  [LATIN SMALL LETTER U WITH BREVE]
        case '\u016F' => 'u' // ů  [LATIN SMALL LETTER U WITH RING ABOVE]
        case '\u0171' => 'u' // ű  [LATIN SMALL LETTER U WITH DOUBLE ACUTE]
        case '\u0173' => 'u' // ų  [LATIN SMALL LETTER U WITH OGONEK]
        case '\u01B0' => 'u' // ư  [LATIN SMALL LETTER U WITH HORN]
        case '\u01D4' => 'u' // ǔ  [LATIN SMALL LETTER U WITH CARON]
        case '\u01D6' => 'u' // ǖ  [LATIN SMALL LETTER U WITH DIAERESIS AND MACRON]
        case '\u01D8' => 'u' // ǘ  [LATIN SMALL LETTER U WITH DIAERESIS AND ACUTE]
        case '\u01DA' => 'u' // ǚ  [LATIN SMALL LETTER U WITH DIAERESIS AND CARON]
        case '\u01DC' => 'u' // ǜ  [LATIN SMALL LETTER U WITH DIAERESIS AND GRAVE]
        case '\u0215' => 'u' // ȕ  [LATIN SMALL LETTER U WITH DOUBLE GRAVE]
        case '\u0217' => 'u' // ȗ  [LATIN SMALL LETTER U WITH INVERTED BREVE]
        case '\u0289' => 'u' // ʉ  [LATIN SMALL LETTER U BAR]
        case '\u1D64' => 'u' // ᵤ  [LATIN SUBSCRIPT SMALL LETTER U]
        case '\u1D99' => 'u' // ᶙ  [LATIN SMALL LETTER U WITH RETROFLEX HOOK]
        case '\u1E73' => 'u' // ṳ  [LATIN SMALL LETTER U WITH DIAERESIS BELOW]
        case '\u1E75' => 'u' // ṵ  [LATIN SMALL LETTER U WITH TILDE BELOW]
        case '\u1E77' => 'u' // ṷ  [LATIN SMALL LETTER U WITH CIRCUMFLEX BELOW]
        case '\u1E79' => 'u' // ṹ  [LATIN SMALL LETTER U WITH TILDE AND ACUTE]
        case '\u1E7B' => 'u' // ṻ  [LATIN SMALL LETTER U WITH MACRON AND DIAERESIS]
        case '\u1EE5' => 'u' // ụ  [LATIN SMALL LETTER U WITH DOT BELOW]
        case '\u1EE7' => 'u' // ủ  [LATIN SMALL LETTER U WITH HOOK ABOVE]
        case '\u1EE9' => 'u' // ứ  [LATIN SMALL LETTER U WITH HORN AND ACUTE]
        case '\u1EEB' => 'u' // ừ  [LATIN SMALL LETTER U WITH HORN AND GRAVE]
        case '\u1EED' => 'u' // ử  [LATIN SMALL LETTER U WITH HORN AND HOOK ABOVE]
        case '\u1EEF' => 'u' // ữ  [LATIN SMALL LETTER U WITH HORN AND TILDE]
        case '\u1EF1' => 'u' // ự  [LATIN SMALL LETTER U WITH HORN AND DOT BELOW]
        case '\u24E4' => 'u' // ⓤ  [CIRCLED LATIN SMALL LETTER U]
        case '\uFF55' => 'u' // ｕ  [FULLWIDTH LATIN SMALL LETTER U]

        case '\u01B2' => 'V' // Ʋ  [LATIN CAPITAL LETTER V WITH HOOK]
        case '\u0245' => 'V' // Ʌ  [LATIN CAPITAL LETTER TURNED V]
        case '\u1D20' => 'V' // ᴠ  [LATIN LETTER SMALL CAPITAL V]
        case '\u1E7C' => 'V' // Ṽ  [LATIN CAPITAL LETTER V WITH TILDE]
        case '\u1E7E' => 'V' // Ṿ  [LATIN CAPITAL LETTER V WITH DOT BELOW]
        case '\u1EFC' => 'V' // Ỽ  [LATIN CAPITAL LETTER MIDDLE-WELSH V]
        case '\u24CB' => 'V' // Ⓥ  [CIRCLED LATIN CAPITAL LETTER V]
        case '\uA75E' => 'V' // Ꝟ  [LATIN CAPITAL LETTER V WITH DIAGONAL STROKE]
        case '\uA768' => 'V' // Ꝩ  [LATIN CAPITAL LETTER VEND]
        case '\uFF36' => 'V' // Ｖ  [FULLWIDTH LATIN CAPITAL LETTER V]

        case '\u028B' => 'v' // ʋ  [LATIN SMALL LETTER V WITH HOOK]
        case '\u028C' => 'v' // ʌ  [LATIN SMALL LETTER TURNED V]
        case '\u1D65' => 'v' // ᵥ  [LATIN SUBSCRIPT SMALL LETTER V]
        case '\u1D8C' => 'v' // ᶌ  [LATIN SMALL LETTER V WITH PALATAL HOOK]
        case '\u1E7D' => 'v' // ṽ  [LATIN SMALL LETTER V WITH TILDE]
        case '\u1E7F' => 'v' // ṿ  [LATIN SMALL LETTER V WITH DOT BELOW]
        case '\u24E5' => 'v' // ⓥ  [CIRCLED LATIN SMALL LETTER V]
        case '\u2C71' => 'v' // ⱱ  [LATIN SMALL LETTER V WITH RIGHT HOOK]
        case '\u2C74' => 'v' // ⱴ  [LATIN SMALL LETTER V WITH CURL]
        case '\uA75F' => 'v' // ꝟ  [LATIN SMALL LETTER V WITH DIAGONAL STROKE]
        case '\uFF56' => 'v' // ｖ  [FULLWIDTH LATIN SMALL LETTER V]

        case '\u0174' => 'W' // Ŵ  [LATIN CAPITAL LETTER W WITH CIRCUMFLEX]
        case '\u01F7' => 'W' // Ƿ  http://en.wikipedia.org/wiki/Wynn  [LATIN CAPITAL LETTER WYNN]
        case '\u1D21' => 'W' // ᴡ  [LATIN LETTER SMALL CAPITAL W]
        case '\u1E80' => 'W' // Ẁ  [LATIN CAPITAL LETTER W WITH GRAVE]
        case '\u1E82' => 'W' // Ẃ  [LATIN CAPITAL LETTER W WITH ACUTE]
        case '\u1E84' => 'W' // Ẅ  [LATIN CAPITAL LETTER W WITH DIAERESIS]
        case '\u1E86' => 'W' // Ẇ  [LATIN CAPITAL LETTER W WITH DOT ABOVE]
        case '\u1E88' => 'W' // Ẉ  [LATIN CAPITAL LETTER W WITH DOT BELOW]
        case '\u24CC' => 'W' // Ⓦ  [CIRCLED LATIN CAPITAL LETTER W]
        case '\u2C72' => 'W' // Ⱳ  [LATIN CAPITAL LETTER W WITH HOOK]
        case '\uFF37' => 'W' // Ｗ  [FULLWIDTH LATIN CAPITAL LETTER W]

        case '\u0175' => 'w' // ŵ  [LATIN SMALL LETTER W WITH CIRCUMFLEX]
        case '\u01BF' => 'w' // ƿ  http://en.wikipedia.org/wiki/Wynn  [LATIN LETTER WYNN]
        case '\u028D' => 'w' // ʍ  [LATIN SMALL LETTER TURNED W]
        case '\u1E81' => 'w' // ẁ  [LATIN SMALL LETTER W WITH GRAVE]
        case '\u1E83' => 'w' // ẃ  [LATIN SMALL LETTER W WITH ACUTE]
        case '\u1E85' => 'w' // ẅ  [LATIN SMALL LETTER W WITH DIAERESIS]
        case '\u1E87' => 'w' // ẇ  [LATIN SMALL LETTER W WITH DOT ABOVE]
        case '\u1E89' => 'w' // ẉ  [LATIN SMALL LETTER W WITH DOT BELOW]
        case '\u1E98' => 'w' // ẘ  [LATIN SMALL LETTER W WITH RING ABOVE]
        case '\u24E6' => 'w' // ⓦ  [CIRCLED LATIN SMALL LETTER W]
        case '\u2C73' => 'w' // ⱳ  [LATIN SMALL LETTER W WITH HOOK]
        case '\uFF57' => 'w' // ｗ  [FULLWIDTH LATIN SMALL LETTER W]

        case '\u1E8A' => 'X' // Ẋ  [LATIN CAPITAL LETTER X WITH DOT ABOVE]
        case '\u1E8C' => 'X' // Ẍ  [LATIN CAPITAL LETTER X WITH DIAERESIS]
        case '\u24CD' => 'X' // Ⓧ  [CIRCLED LATIN CAPITAL LETTER X]
        case '\uFF38' => 'X' // Ｘ  [FULLWIDTH LATIN CAPITAL LETTER X]

        case '\u1D8D' => 'x' // ᶍ  [LATIN SMALL LETTER X WITH PALATAL HOOK]
        case '\u1E8B' => 'x' // ẋ  [LATIN SMALL LETTER X WITH DOT ABOVE]
        case '\u1E8D' => 'x' // ẍ  [LATIN SMALL LETTER X WITH DIAERESIS]
        case '\u2093' => 'x' // ₓ  [LATIN SUBSCRIPT SMALL LETTER X]
        case '\u24E7' => 'x' // ⓧ  [CIRCLED LATIN SMALL LETTER X]
        case '\uFF58' => 'x' // ｘ  [FULLWIDTH LATIN SMALL LETTER X]

        case '\u00DD' => 'Y' // Ý  [LATIN CAPITAL LETTER Y WITH ACUTE]
        case '\u0176' => 'Y' // Ŷ  [LATIN CAPITAL LETTER Y WITH CIRCUMFLEX]
        case '\u0178' => 'Y' // Ÿ  [LATIN CAPITAL LETTER Y WITH DIAERESIS]
        case '\u01B3' => 'Y' // Ƴ  [LATIN CAPITAL LETTER Y WITH HOOK]
        case '\u0232' => 'Y' // Ȳ  [LATIN CAPITAL LETTER Y WITH MACRON]
        case '\u024E' => 'Y' // Ɏ  [LATIN CAPITAL LETTER Y WITH STROKE]
        case '\u028F' => 'Y' // ʏ  [LATIN LETTER SMALL CAPITAL Y]
        case '\u1E8E' => 'Y' // Ẏ  [LATIN CAPITAL LETTER Y WITH DOT ABOVE]
        case '\u1EF2' => 'Y' // Ỳ  [LATIN CAPITAL LETTER Y WITH GRAVE]
        case '\u1EF4' => 'Y' // Ỵ  [LATIN CAPITAL LETTER Y WITH DOT BELOW]
        case '\u1EF6' => 'Y' // Ỷ  [LATIN CAPITAL LETTER Y WITH HOOK ABOVE]
        case '\u1EF8' => 'Y' // Ỹ  [LATIN CAPITAL LETTER Y WITH TILDE]
        case '\u1EFE' => 'Y' // Ỿ  [LATIN CAPITAL LETTER Y WITH LOOP]
        case '\u24CE' => 'Y' // Ⓨ  [CIRCLED LATIN CAPITAL LETTER Y]
        case '\uFF39' => 'Y' // Ｙ  [FULLWIDTH LATIN CAPITAL LETTER Y]

        case '\u00FD' => 'y' // ý  [LATIN SMALL LETTER Y WITH ACUTE]
        case '\u00FF' => 'y' // ÿ  [LATIN SMALL LETTER Y WITH DIAERESIS]
        case '\u0177' => 'y' // ŷ  [LATIN SMALL LETTER Y WITH CIRCUMFLEX]
        case '\u01B4' => 'y' // ƴ  [LATIN SMALL LETTER Y WITH HOOK]
        case '\u0233' => 'y' // ȳ  [LATIN SMALL LETTER Y WITH MACRON]
        case '\u024F' => 'y' // ɏ  [LATIN SMALL LETTER Y WITH STROKE]
        case '\u028E' => 'y' // ʎ  [LATIN SMALL LETTER TURNED Y]
        case '\u1E8F' => 'y' // ẏ  [LATIN SMALL LETTER Y WITH DOT ABOVE]
        case '\u1E99' => 'y' // ẙ  [LATIN SMALL LETTER Y WITH RING ABOVE]
        case '\u1EF3' => 'y' // ỳ  [LATIN SMALL LETTER Y WITH GRAVE]
        case '\u1EF5' => 'y' // ỵ  [LATIN SMALL LETTER Y WITH DOT BELOW]
        case '\u1EF7' => 'y' // ỷ  [LATIN SMALL LETTER Y WITH HOOK ABOVE]
        case '\u1EF9' => 'y' // ỹ  [LATIN SMALL LETTER Y WITH TILDE]
        case '\u1EFF' => 'y' // ỿ  [LATIN SMALL LETTER Y WITH LOOP]
        case '\u24E8' => 'y' // ⓨ  [CIRCLED LATIN SMALL LETTER Y]
        case '\uFF59' => 'y' // ｙ  [FULLWIDTH LATIN SMALL LETTER Y]

        case '\u0179' => 'Z' // Ź  [LATIN CAPITAL LETTER Z WITH ACUTE]
        case '\u017B' => 'Z' // Ż  [LATIN CAPITAL LETTER Z WITH DOT ABOVE]
        case '\u017D' => 'Z' // Ž  [LATIN CAPITAL LETTER Z WITH CARON]
        case '\u01B5' => 'Z' // Ƶ  [LATIN CAPITAL LETTER Z WITH STROKE]
        case '\u021C' => 'Z' // Ȝ  http://en.wikipedia.org/wiki/Yogh  [LATIN CAPITAL LETTER YOGH]
        case '\u0224' => 'Z' // Ȥ  [LATIN CAPITAL LETTER Z WITH HOOK]
        case '\u1D22' => 'Z' // ᴢ  [LATIN LETTER SMALL CAPITAL Z]
        case '\u1E90' => 'Z' // Ẑ  [LATIN CAPITAL LETTER Z WITH CIRCUMFLEX]
        case '\u1E92' => 'Z' // Ẓ  [LATIN CAPITAL LETTER Z WITH DOT BELOW]
        case '\u1E94' => 'Z' // Ẕ  [LATIN CAPITAL LETTER Z WITH LINE BELOW]
        case '\u24CF' => 'Z' // Ⓩ  [CIRCLED LATIN CAPITAL LETTER Z]
        case '\u2C6B' => 'Z' // Ⱬ  [LATIN CAPITAL LETTER Z WITH DESCENDER]
        case '\uA762' => 'Z' // Ꝣ  [LATIN CAPITAL LETTER VISIGOTHIC Z]
        case '\uFF3A' => 'Z' // Ｚ  [FULLWIDTH LATIN CAPITAL LETTER Z]

        case '\u017A' => 'z' // ź  [LATIN SMALL LETTER Z WITH ACUTE]
        case '\u017C' => 'z' // ż  [LATIN SMALL LETTER Z WITH DOT ABOVE]
        case '\u017E' => 'z' // ž  [LATIN SMALL LETTER Z WITH CARON]
        case '\u01B6' => 'z' // ƶ  [LATIN SMALL LETTER Z WITH STROKE]
        case '\u021D' => 'z' // ȝ  http://en.wikipedia.org/wiki/Yogh  [LATIN SMALL LETTER YOGH]
        case '\u0225' => 'z' // ȥ  [LATIN SMALL LETTER Z WITH HOOK]
        case '\u0240' => 'z' // ɀ  [LATIN SMALL LETTER Z WITH SWASH TAIL]
        case '\u0290' => 'z' // ʐ  [LATIN SMALL LETTER Z WITH RETROFLEX HOOK]
        case '\u0291' => 'z' // ʑ  [LATIN SMALL LETTER Z WITH CURL]
        case '\u1D76' => 'z' // ᵶ  [LATIN SMALL LETTER Z WITH MIDDLE TILDE]
        case '\u1D8E' => 'z' // ᶎ  [LATIN SMALL LETTER Z WITH PALATAL HOOK]
        case '\u1E91' => 'z' // ẑ  [LATIN SMALL LETTER Z WITH CIRCUMFLEX]
        case '\u1E93' => 'z' // ẓ  [LATIN SMALL LETTER Z WITH DOT BELOW]
        case '\u1E95' => 'z' // ẕ  [LATIN SMALL LETTER Z WITH LINE BELOW]
        case '\u24E9' => 'z' // ⓩ  [CIRCLED LATIN SMALL LETTER Z]
        case '\u2C6C' => 'z' // ⱬ  [LATIN SMALL LETTER Z WITH DESCENDER]
        case '\uA763' => 'z' // ꝣ  [LATIN SMALL LETTER VISIGOTHIC Z]
        case '\uFF5A' => 'z' // ｚ  [FULLWIDTH LATIN SMALL LETTER Z]

        case '\u2070' => '0' // ⁰  [SUPERSCRIPT ZERO]
        case '\u2080' => '0' // ₀  [SUBSCRIPT ZERO]
        case '\u24EA' => '0' // ⓪  [CIRCLED DIGIT ZERO]
        case '\u24FF' => '0' // ⓿  [NEGATIVE CIRCLED DIGIT ZERO]
        case '\uFF10' => '0' // ０  [FULLWIDTH DIGIT ZERO]

        case '\u00B9' => '1' // ¹  [SUPERSCRIPT ONE]
        case '\u2081' => '1' // ₁  [SUBSCRIPT ONE]
        case '\u2460' => '1' // ①  [CIRCLED DIGIT ONE]
        case '\u24F5' => '1' // ⓵  [DOUBLE CIRCLED DIGIT ONE]
        case '\u2776' => '1' // ❶  [DINGBAT NEGATIVE CIRCLED DIGIT ONE]
        case '\u2780' => '1' // ➀  [DINGBAT CIRCLED SANS-SERIF DIGIT ONE]
        case '\u278A' => '1' // ➊  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT ONE]
        case '\uFF11' => '1' // １  [FULLWIDTH DIGIT ONE]

        case '\u00B2' => '2' // ²  [SUPERSCRIPT TWO]
        case '\u2082' => '2' // ₂  [SUBSCRIPT TWO]
        case '\u2461' => '2' // ②  [CIRCLED DIGIT TWO]
        case '\u24F6' => '2' // ⓶  [DOUBLE CIRCLED DIGIT TWO]
        case '\u2777' => '2' // ❷  [DINGBAT NEGATIVE CIRCLED DIGIT TWO]
        case '\u2781' => '2' // ➁  [DINGBAT CIRCLED SANS-SERIF DIGIT TWO]
        case '\u278B' => '2' // ➋  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT TWO]
        case '\uFF12' => '2' // ２  [FULLWIDTH DIGIT TWO]

        case '\u00B3' => '3' // ³  [SUPERSCRIPT THREE]
        case '\u2083' => '3' // ₃  [SUBSCRIPT THREE]
        case '\u2462' => '3' // ③  [CIRCLED DIGIT THREE]
        case '\u24F7' => '3' // ⓷  [DOUBLE CIRCLED DIGIT THREE]
        case '\u2778' => '3' // ❸  [DINGBAT NEGATIVE CIRCLED DIGIT THREE]
        case '\u2782' => '3' // ➂  [DINGBAT CIRCLED SANS-SERIF DIGIT THREE]
        case '\u278C' => '3' // ➌  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT THREE]
        case '\uFF13' => '3' // ３  [FULLWIDTH DIGIT THREE]

        case '\u2074' => '4' // ⁴  [SUPERSCRIPT FOUR]
        case '\u2084' => '4' // ₄  [SUBSCRIPT FOUR]
        case '\u2463' => '4' // ④  [CIRCLED DIGIT FOUR]
        case '\u24F8' => '4' // ⓸  [DOUBLE CIRCLED DIGIT FOUR]
        case '\u2779' => '4' // ❹  [DINGBAT NEGATIVE CIRCLED DIGIT FOUR]
        case '\u2783' => '4' // ➃  [DINGBAT CIRCLED SANS-SERIF DIGIT FOUR]
        case '\u278D' => '4' // ➍  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT FOUR]
        case '\uFF14' => '4' // ４  [FULLWIDTH DIGIT FOUR]

        case '\u2075' => '5' // ⁵  [SUPERSCRIPT FIVE]
        case '\u2085' => '5' // ₅  [SUBSCRIPT FIVE]
        case '\u2464' => '5' // ⑤  [CIRCLED DIGIT FIVE]
        case '\u24F9' => '5' // ⓹  [DOUBLE CIRCLED DIGIT FIVE]
        case '\u277A' => '5' // ❺  [DINGBAT NEGATIVE CIRCLED DIGIT FIVE]
        case '\u2784' => '5' // ➄  [DINGBAT CIRCLED SANS-SERIF DIGIT FIVE]
        case '\u278E' => '5' // ➎  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT FIVE]
        case '\uFF15' => '5' // ５  [FULLWIDTH DIGIT FIVE]

        case '\u2076' => '6' // ⁶  [SUPERSCRIPT SIX]
        case '\u2086' => '6' // ₆  [SUBSCRIPT SIX]
        case '\u2465' => '6' // ⑥  [CIRCLED DIGIT SIX]
        case '\u24FA' => '6' // ⓺  [DOUBLE CIRCLED DIGIT SIX]
        case '\u277B' => '6' // ❻  [DINGBAT NEGATIVE CIRCLED DIGIT SIX]
        case '\u2785' => '6' // ➅  [DINGBAT CIRCLED SANS-SERIF DIGIT SIX]
        case '\u278F' => '6' // ➏  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT SIX]
        case '\uFF16' => '6' // ６  [FULLWIDTH DIGIT SIX]

        case '\u2077' => '7' // ⁷  [SUPERSCRIPT SEVEN]
        case '\u2087' => '7' // ₇  [SUBSCRIPT SEVEN]
        case '\u2466' => '7' // ⑦  [CIRCLED DIGIT SEVEN]
        case '\u24FB' => '7' // ⓻  [DOUBLE CIRCLED DIGIT SEVEN]
        case '\u277C' => '7' // ❼  [DINGBAT NEGATIVE CIRCLED DIGIT SEVEN]
        case '\u2786' => '7' // ➆  [DINGBAT CIRCLED SANS-SERIF DIGIT SEVEN]
        case '\u2790' => '7' // ➐  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT SEVEN]
        case '\uFF17' => '7' // ７  [FULLWIDTH DIGIT SEVEN]

        case '\u2078' => '8' // ⁸  [SUPERSCRIPT EIGHT]
        case '\u2088' => '8' // ₈  [SUBSCRIPT EIGHT]
        case '\u2467' => '8' // ⑧  [CIRCLED DIGIT EIGHT]
        case '\u24FC' => '8' // ⓼  [DOUBLE CIRCLED DIGIT EIGHT]
        case '\u277D' => '8' // ❽  [DINGBAT NEGATIVE CIRCLED DIGIT EIGHT]
        case '\u2787' => '8' // ➇  [DINGBAT CIRCLED SANS-SERIF DIGIT EIGHT]
        case '\u2791' => '8' // ➑  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT EIGHT]
        case '\uFF18' => '8' // ８  [FULLWIDTH DIGIT EIGHT]

        case '\u2079' => '9' // ⁹  [SUPERSCRIPT NINE]
        case '\u2089' => '9' // ₉  [SUBSCRIPT NINE]
        case '\u2468' => '9' // ⑨  [CIRCLED DIGIT NINE]
        case '\u24FD' => '9' // ⓽  [DOUBLE CIRCLED DIGIT NINE]
        case '\u277E' => '9' // ❾  [DINGBAT NEGATIVE CIRCLED DIGIT NINE]
        case '\u2788' => '9' // ➈  [DINGBAT CIRCLED SANS-SERIF DIGIT NINE]
        case '\u2792' => '9' // ➒  [DINGBAT NEGATIVE CIRCLED SANS-SERIF DIGIT NINE]
        case '\uFF19' => '9' // ９  [FULLWIDTH DIGIT NINE]

        case '\u00AB' => '"' // «  [LEFT-POINTING DOUBLE ANGLE QUOTATION MARK]
        case '\u00BB' => '"' // »  [RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK]
        case '\u201C' => '"' // “  [LEFT DOUBLE QUOTATION MARK]
        case '\u201D' => '"' // ”  [RIGHT DOUBLE QUOTATION MARK]
        case '\u201E' => '"' // „  [DOUBLE LOW-9 QUOTATION MARK]
        case '\u2033' => '"' // ″  [DOUBLE PRIME]
        case '\u2036' => '"' // ‶  [REVERSED DOUBLE PRIME]
        case '\u275D' => '"' // ❝  [HEAVY DOUBLE TURNED COMMA QUOTATION MARK ORNAMENT]
        case '\u275E' => '"' // ❞  [HEAVY DOUBLE COMMA QUOTATION MARK ORNAMENT]
        case '\u276E' => '"' // ❮  [HEAVY LEFT-POINTING ANGLE QUOTATION MARK ORNAMENT]
        case '\u276F' => '"' // ❯  [HEAVY RIGHT-POINTING ANGLE QUOTATION MARK ORNAMENT]
        case '\uFF02' => '"' // ＂  [FULLWIDTH QUOTATION MARK]

        case '\u2018' => '\'' // ‘  [LEFT SINGLE QUOTATION MARK]
        case '\u2019' => '\'' // ’  [RIGHT SINGLE QUOTATION MARK]
        case '\u201A' => '\'' // ‚  [SINGLE LOW-9 QUOTATION MARK]
        case '\u201B' => '\'' // ‛  [SINGLE HIGH-REVERSED-9 QUOTATION MARK]
        case '\u2032' => '\'' // ′  [PRIME]
        case '\u2035' => '\'' // ‵  [REVERSED PRIME]
        case '\u2039' => '\'' // ‹  [SINGLE LEFT-POINTING ANGLE QUOTATION MARK]
        case '\u203A' => '\'' // ›  [SINGLE RIGHT-POINTING ANGLE QUOTATION MARK]
        case '\u275B' => '\'' // ❛  [HEAVY SINGLE TURNED COMMA QUOTATION MARK ORNAMENT]
        case '\u275C' => '\'' // ❜  [HEAVY SINGLE COMMA QUOTATION MARK ORNAMENT]
        case '\uFF07' => '\'' // ＇  [FULLWIDTH APOSTROPHE]

        case '\u2010' => '-' // ‐  [HYPHEN]
        case '\u2011' => '-' // ‑  [NON-BREAKING HYPHEN]
        case '\u2012' => '-' // ‒  [FIGURE DASH]
        case '\u2013' => '-' // –  [EN DASH]
        case '\u2014' => '-' // —  [EM DASH]
        case '\u207B' => '-' // ⁻  [SUPERSCRIPT MINUS]
        case '\u208B' => '-' // ₋  [SUBSCRIPT MINUS]
        case '\uFF0D' => '-' // －  [FULLWIDTH HYPHEN-MINUS]

        case '\u2045' => '[' // ⁅  [LEFT SQUARE BRACKET WITH QUILL]
        case '\u2772' => '[' // ❲  [LIGHT LEFT TORTOISE SHELL BRACKET ORNAMENT]
        case '\uFF3B' => '[' // ［  [FULLWIDTH LEFT SQUARE BRACKET]

        case '\u2046' => ']' // ⁆  [RIGHT SQUARE BRACKET WITH QUILL]
        case '\u2773' => ']' // ❳  [LIGHT RIGHT TORTOISE SHELL BRACKET ORNAMENT]
        case '\uFF3D' => ']' // ］  [FULLWIDTH RIGHT SQUARE BRACKET]

        case '\u207D' => '(' // ⁽  [SUPERSCRIPT LEFT PARENTHESIS]
        case '\u208D' => '(' // ₍  [SUBSCRIPT LEFT PARENTHESIS]
        case '\u2768' => '(' // ❨  [MEDIUM LEFT PARENTHESIS ORNAMENT]
        case '\u276A' => '(' // ❪  [MEDIUM FLATTENED LEFT PARENTHESIS ORNAMENT]
        case '\uFF08' => '(' // （  [FULLWIDTH LEFT PARENTHESIS]

        case '\u207E' => ')' // ⁾  [SUPERSCRIPT RIGHT PARENTHESIS]
        case '\u208E' => ')' // ₎  [SUBSCRIPT RIGHT PARENTHESIS]
        case '\u2769' => ')' // ❩  [MEDIUM RIGHT PARENTHESIS ORNAMENT]
        case '\u276B' => ')' // ❫  [MEDIUM FLATTENED RIGHT PARENTHESIS ORNAMENT]
        case '\uFF09' => ')' // ）  [FULLWIDTH RIGHT PARENTHESIS]

        case '\u276C' => '<' // ❬  [MEDIUM LEFT-POINTING ANGLE BRACKET ORNAMENT]
        case '\u2770' => '<' // ❰  [HEAVY LEFT-POINTING ANGLE BRACKET ORNAMENT]
        case '\uFF1C' => '<' // ＜  [FULLWIDTH LESS-THAN SIGN]

        case '\u276D' => '>' // ❭  [MEDIUM RIGHT-POINTING ANGLE BRACKET ORNAMENT]
        case '\u2771' => '>' // ❱  [HEAVY RIGHT-POINTING ANGLE BRACKET ORNAMENT]
        case '\uFF1E' => '>' // ＞  [FULLWIDTH GREATER-THAN SIGN]

        case '\u2774' => '{' // ❴  [MEDIUM LEFT CURLY BRACKET ORNAMENT]
        case '\uFF5B' => '{' // ｛  [FULLWIDTH LEFT CURLY BRACKET]

        case '\u2775' => '}' // ❵  [MEDIUM RIGHT CURLY BRACKET ORNAMENT]
        case '\uFF5D' => '}' // ｝  [FULLWIDTH RIGHT CURLY BRACKET]

        case '\u207A' => '+' // ⁺  [SUPERSCRIPT PLUS SIGN]
        case '\u208A' => '+' // ₊  [SUBSCRIPT PLUS SIGN]
        case '\uFF0B' => '+' // ＋  [FULLWIDTH PLUS SIGN]

        case '\u207C' => '=' // ⁼  [SUPERSCRIPT EQUALS SIGN]
        case '\u208C' => '=' // ₌  [SUBSCRIPT EQUALS SIGN]
        case '\uFF1D' => '=' // ＝  [FULLWIDTH EQUALS SIGN]

        case '\uFF01' => '!' // ！  [FULLWIDTH EXCLAMATION MARK]

        case '\uFF03' => '#' // ＃  [FULLWIDTH NUMBER SIGN]

        case '\uFF04' => '$' // ＄  [FULLWIDTH DOLLAR SIGN]

        case '\u2052' => '%' // ⁒  [COMMERCIAL MINUS SIGN]
        case '\uFF05' => '%' // ％  [FULLWIDTH PERCENT SIGN]

        case '\uFF06' => '&' // ＆  [FULLWIDTH AMPERSAND]

        case '\u204E' => '*' // ⁎  [LOW ASTERISK]
        case '\uFF0A' => '*' // ＊  [FULLWIDTH ASTERISK]

        case '\uFF0C' => ',' // ，  [FULLWIDTH COMMA]

        case '\uFF0E' => '.' // ．  [FULLWIDTH FULL STOP]

        case '\u2044' => '/' // ⁄  [FRACTION SLASH]
        case '\uFF0F' => '/' // ／  [FULLWIDTH SOLIDUS]

        case '\uFF1A' => ':' // ：  [FULLWIDTH COLON]

        case '\u204F' => ';' // ⁏  [REVERSED SEMICOLON]
        case '\uFF1B' => ';' // ；  [FULLWIDTH SEMICOLON]

        case '\uFF1F' => '?' // ？  [FULLWIDTH QUESTION MARK]

        case '\uFF20' => '@' // ＠  [FULLWIDTH COMMERCIAL AT]

        case '\uFF3C' => '\\' // ＼  [FULLWIDTH REVERSE SOLIDUS]

        case '\u2038' => '^' // ‸  [CARET]
        case '\uFF3E' => '^' // ＾  [FULLWIDTH CIRCUMFLEX ACCENT]

        case '\uFF3F' => '_' // ＿  [FULLWIDTH LOW LINE]

        case '\u2053' => '~' // ⁓  [SWUNG DASH]
        case '\uFF5E' => '~' // ～  [FULLWIDTH TILDE]
        
        case _ => c
      }
    }
  }
}
