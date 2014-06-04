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

import org.apache.commons.codec.binary.{Hex => Apache}

/**
 * Wrappers around org.apache.commons.codec.binary.Hex
 */
object Hex {
  /** Converts an array of characters representing hexadecimal values into an array of bytes of those same values. */
  def decodeHex(data: Array[Char]): Array[Byte] = Apache.decodeHex(data)
  
  /** Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order. */
  def encodeHex(data: Array[Byte]): Array[Char] = Apache.encodeHex(data)
  
  /** Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.*/
  def encodeHex(data: Array[Byte], toLowerCase: Boolean): Array[Char] = Apache.encodeHex(data, toLowerCase)
  
  /** Converts an array of bytes into a String representing the hexadecimal values of each byte in order. */
  def encodeHexString(data: Array[Byte]): String = Apache.encodeHexString(data)
}