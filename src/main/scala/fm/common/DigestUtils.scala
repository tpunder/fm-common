package fm.common

import java.io.InputStream
import org.apache.commons.codec.digest.{DigestUtils => Apache}

/**
 * Simple Wrapper around org.apache.commons.codec.digest.DigestUtils
 */
object DigestUtils {
  def md5(data: Array[Byte]): Array[Byte] = Apache.md5(data)
  def md5(data: InputStream): Array[Byte] = Apache.md5(data)
  def md5(data: String)     : Array[Byte] = Apache.md5(data)
  
  def md5Hex(data: Array[Byte]): String = Apache.md5Hex(data)
  def md5Hex(data: InputStream): String = Apache.md5Hex(data)
  def md5Hex(data: String)     : String = Apache.md5Hex(data)
  
  def sha1(data: Array[Byte]): Array[Byte] = Apache.sha1(data)
  def sha1(data: InputStream): Array[Byte] = Apache.sha1(data)
  def sha1(data: String)     : Array[Byte] = Apache.sha1(data)
  
  def sha1Hex(data: Array[Byte]): String = Apache.sha1Hex(data)
  def sha1Hex(data: InputStream): String = Apache.sha1Hex(data)
  def sha1Hex(data: String)     : String = Apache.sha1Hex(data)
  
  def sha256(data: Array[Byte]): Array[Byte] = Apache.sha256(data)
  def sha256(data: InputStream): Array[Byte] = Apache.sha256(data)
  def sha256(data: String)     : Array[Byte] = Apache.sha256(data)
  
  def sha256Hex(data: Array[Byte]): String = Apache.sha256Hex(data)
  def sha256Hex(data: InputStream): String = Apache.sha256Hex(data)
  def sha256Hex(data: String)     : String = Apache.sha256Hex(data)
}