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

object BitUtils {
  /**
   * Create a long out of 2 ints such that the first int is the upper 32 bits of the long
   * and the second int is the lower 32 bits of the long.
   * 
   * [a - upper 32 bits][b - lower 32 bits]
   */
  def makeLong(a: Int, b: Int): Long = ((a: Long) << 32) | (b & 0xffffffffL)

  def makeInt(a: Short, b: Short): Int = ((a: Int) << 16) | (b & 0xffff)
  
  /**
   * Split a long into 2 ints (the reverse of makeLong())
   */
  def splitLong(long: Long): (Int, Int) = (getUpper(long), getLower(long))

  /**
   * Split an int into 2 shorts (the reverse of makeInt())
   */
  def splitInt(int: Int): (Short, Short) = (getUpper(int), getLower(int))
  
  /**
   * Get the upper 32 bits of the long
   */
  def getUpper(long: Long): Int = (long >> 32).toInt

  /**
   * Get the upper 16 bits of the int
   */
  def getUpper(int: Int): Short = (int >> 16).toShort

  /**
   * Get the lower 32 bits of the long
   */
  def getLower(long: Long): Int = long.toInt

  /**
   * Get the lower 16 bits of the int
   */
  def getLower(int: Int): Short = int.toShort

}