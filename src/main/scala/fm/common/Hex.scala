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