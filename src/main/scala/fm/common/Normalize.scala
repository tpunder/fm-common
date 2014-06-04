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
package fm.common

import scala.collection.mutable.{ArrayBuffer,Builder}
import fm.common.Implicits._

object Normalize {
  /**
   * Replaces any non-alphanumeric characters with collapsed spaces
   */
  def lowerAlphanumericWithSpaces(s: String): String = {
    if (null == s) return ""
    
    val sb = new java.lang.StringBuilder
    
    var i: Int = 0
    var prevCh: Char = 0
    while(i < s.length) {
      val ch = s.charAt(i)
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
   * Removes any non-alphanumeric characters - Only allocates a new string if the passed in string is not already normalized
   * 
   * Note: This logic should match reverseLowerAlphanumeric() -- EXCEPT that this implementation now only allocates if it needs to
   */
  def lowerAlphanumeric(s: String): String = {
    if (null == s) return ""
    
    var arr: Array[Char] = null
    var arrIdx: Int = 0
    
    var i: Int = 0
    while (i < s.length) {
      val ch: Char = s.charAt(i)
      
      if (null == arr && (!Character.isLetterOrDigit(ch) || ch != Character.toLowerCase(ch))) {
        // The original string is not normalized so we need to initialize arr and copy over everything so far
        arr = new Array[Char](s.length)
        
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
          arrIdx += 1
        }
      }
      
      i += 1
    }
    
    // If arr is null then the original string is already normalized
    if (null == arr) s else new String(arr, 0, arrIdx)
  }
    
  /**
   * Given the original string and a normalized substring, extract the original version of the normalized substring.
   * e.g. Original: "Foo B.O.S.C.H. Bar"  Normalized: "bosch"  Result: "B.O.S.C.H."
   * 
   * Note: This logic should match lowerAlphanumeric
   */
  def reverseLowerAlphanumeric(original: String, normalized: String): Option[String] = {
    if (original.isBlank || normalized.isBlank) return None
    
    val arr: Array[Char] = new Array[Char](original.length)
    var arrIdx: Int = 0
    
    val positions: Array[Int] = new Array[Int](original.length)
    
    var i: Int = 0
    while (i < original.length) {
      val ch: Char = original.charAt(i)
      if(Character.isLetterOrDigit(ch)) {
        arr(arrIdx) = Character.toLowerCase(ch)
        positions(arrIdx) = i
        arrIdx += 1
      }
      i += 1
    }
    
    val normalizedOriginal: String = new String(arr, 0, arrIdx)
    
    //println("arr: "+arr.toIndexedSeq)
    //println("positions: "+positions.toIndexedSeq)
    
    val matchIdx: Int = normalizedOriginal.indexOf(normalized)
    if (matchIdx < 0) None else {
      val startIdx: Int = positions(matchIdx)
      
      var endIdx: Int = positions(matchIdx + normalized.length - 1)
      val maxEndIdx: Int = if (matchIdx + normalized.length >= normalizedOriginal.length) original.length else positions(matchIdx + normalized.length)
      
      // Take any additional non-whitespace up to the next normalized character
      while(endIdx < maxEndIdx && !Character.isWhitespace(original.charAt(endIdx))) {
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
    val size = s.length
    var i = 0

    var sb = new StringBuilder
    
    while(i < size) {
      val ch = s(i)
      
      // If its a valid character (alphanumeric or a dot) add it to the StringBuilder
      if(Character.isLetterOrDigit(ch) || ch == '.') {
        sb.append(Character.toLowerCase(ch))
      } else if(sb.length > 0) {
        // Otherwise we have a complete word, add it to the result buffer
        buf += sb.toString
        sb = new StringBuilder
      }
      
      i += 1
    }

    // If there is anything left in the StringBuilder, add it to the result buffer
    if(sb.length > 0) buf += sb.toString
  }

  def stripControl(s: String): String = {
    new String(s.filter{ch => !Character.isISOControl(ch) || '\t' == ch }.toArray)
  }

  def numeric(s: String): String = {
    new String(s.filter{ch => Character.isDigit(ch) || '.' == ch || '-' == ch }.toArray)
  }
}
