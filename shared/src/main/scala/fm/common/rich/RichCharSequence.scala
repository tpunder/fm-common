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

import fm.common.{ImmutableArray, ImmutableArrayBuilder}

/**
 * Provides additional functionality for java.lang.CharSequence
 */
final class RichCharSequence(val s: CharSequence) extends AnyVal {

  /**
   * Returns true if the string is null or only whitespace
   */
  def isBlank: Boolean = {
    if (null == s) return true
    
    var i: Int = 0
    val len: Int = s.length()
    while (i < len) {
      if (!Character.isWhitespace(s.charAt(i))) return false 
      i += 1
    }
    
    true
  }

  /**
   * Opposite of isBlank
   */
  def isNotBlank: Boolean = !isBlank
  
  /**
   * Opposite of isBlank (alias for isNotBlank)
   */
  def nonBlank: Boolean = !isBlank

  /**
   * Do the next characters starting at idx match the target
   */
  def nextCharsMatch(target: CharSequence, idx: Int = 0): Boolean = {
    require(idx >= 0, s"RichSequence.nextCharsMatch - Negative Idx: $idx")
    
    if(null == target || target.length == 0) return false

    var i: Int = 0
    while(i < target.length && i+idx < s.length && target.charAt(i) == s.charAt(i+idx)) {
      i += 1
    }

    i == target.length
  }

  /**
   * Same as String.startsWith(prefix) but for a CharSequence
   */
  def startsWith(target: CharSequence): Boolean = nextCharsMatch(target)
  
  /**
   * Count the occurrences of the character
   */
  def countOccurrences(ch: Char): Int = {
    var count: Int = 0
    var i: Int = 0
    
    while (i < s.length) {
      if (s.charAt(i) == ch) count += 1
      i += 1
    }
    
    count
  }
  
  def indexesOf(target: CharSequence, withOverlaps: Boolean): IndexedSeq[Int] = indexesOf(target, 0, withOverlaps)
  
  def indexesOf(target: CharSequence, fromIdx: Int, withOverlaps: Boolean): IndexedSeq[Int] = {
    if (target == null) return ImmutableArray.empty[Int]
    
    val builder = new ImmutableArrayBuilder[Int](0)
    
    var i: Int = fromIdx
    
    while (i < s.length) {
      if (nextCharsMatch(target, i)) {
        builder += i
        i += (if (withOverlaps) 1 else target.length)
      } else {
        i += 1
      }
    }
   
    builder.result
  }
  
  def matches(pattern: java.util.regex.Pattern): Boolean = pattern.matcher(s).matches()
  def matches(regex: scala.util.matching.Regex): Boolean = regex.pattern.matcher(s).matches()
}