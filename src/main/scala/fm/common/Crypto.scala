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

import org.bouncycastle.crypto.{BufferedBlockCipher, Digest, Mac}
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.digests.{SHA1Digest, SHA256Digest}
import org.bouncycastle.crypto.engines.AESFastEngine
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.{PaddedBufferedBlockCipher, PKCS7Padding}
import org.bouncycastle.crypto.params.{KeyParameter, ParametersWithIV}
import java.nio.charset.StandardCharsets.UTF_8
import java.security.SecureRandom

object Crypto {
  private val DefaultCipher: BufferedBlockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine), new PKCS7Padding)
  private val DefaultMac: Mac = new HMac(new SHA1Digest)
  private val DefaultKeyLengthBits: Int = 256
  
  def makeRandomKeyBase64(bits: Int): String = makeRandomKeyBase64(bits, urlSafe = false)
  
  def makeRandomKeyBase64URLSafe(bits: Int): String = makeRandomKeyBase64(bits, urlSafe = true)
  
  def makeRandomKeyBase64(bits: Int, urlSafe: Boolean): String = Base64.encodeBytes(makeRandomKey(bits), if (urlSafe) Base64.URL_SAFE else Base64.NO_OPTIONS)
  
  def makeRandomKey(bits: Int): Array[Byte] = {
    require(bits % 8 == 0, "bits should be a multiple of 8")
    val bytes = new Array[Byte](bits / 8)
    new SecureRandom().nextBytes(bytes)
    bytes
  }
}

// Proguard doesn't like this setup:
//final class Crypto private (key: Array[Byte], keyLengthBits: Int, cipher: BufferedBlockCipher, mac: Mac) {
//  def this(key: Array[Byte]) = this(key, Crypto.DefaultKeyLengthBits, Crypto.DefaultCipher, Crypto.DefaultMac)
//  def this(base64Key: String) = this(Base64.decode(base64Key))
//  private def this(base64Key: String, keyLengthBits: Int, cipher: BufferedBlockCipher, mac: Mac) = this(Base64.decode(base64Key), keyLengthBits, cipher, mac)
//
final class Crypto (key: Array[Byte]) {
  def this(base64Key: String) = this(Base64.decode(base64Key))
  
  private[this] val keyLengthBits: Int = Crypto.DefaultKeyLengthBits
  private[this] val cipher: BufferedBlockCipher = Crypto.DefaultCipher
  private[this] val mac: Mac = Crypto.DefaultMac
  private[this] val secureRandom: SecureRandom = new SecureRandom()
  
  private def ENCRYPT: Boolean = true
  private def DECRYPT: Boolean = false
    
  private def keyLenBytes: Int = {
    require(keyLengthBits % 8 == 0, "keyLengthBits should be a multiple of 8")
    keyLengthBits / 8
  }
  
  private[this] val keyBytes: Array[Byte] = {
    if(key.length == keyLenBytes) key
    else if(key.length > keyLenBytes) key.slice(0, keyLenBytes) // truncate
    // NOTE: this be replaced with a proper key derivation function but we first need to figure out if any production code relies on this functionality.
    //       Some tests in MessageCrypto rely on it but I don't think any production code does.
    else sha256(key) // The key is too short, take the sha256 hash of it and use that.
  }
  
  def encryptBase64String(plaintext: String): String = encryptBase64String(plaintext, urlSafe = false)
  
  def encryptBase64StringURLSafe(plaintext: String): String = encryptBase64String(plaintext, urlSafe = true)
  
  def encryptBase64String(plaintext: String, urlSafe: Boolean): String = {
    Base64.encodeBytes(encrypt(plaintext.getBytes(UTF_8)), if (urlSafe) Base64.URL_SAFE else Base64.NO_OPTIONS)
  }
  
  /** Encrypt bytes returning the iv and ciphertext combined into a single byte array (iv followed by the cipher text) */
  def encrypt(plaintext: Array[Byte]): Array[Byte] = {
    val (iv, ciphertext) = encryptRaw(plaintext)
    val bytes = new Array[Byte](iv.length + ciphertext.length)
    System.arraycopy(iv, 0, bytes, 0, iv.length)
    System.arraycopy(ciphertext, 0, bytes, iv.length, ciphertext.length)
    bytes
  }
  
  /** Encrypt a string returning the tuple: (iv, ciphertext) */
  def encryptRaw(plaintext: String): (Array[Byte], Array[Byte]) = {
    require(null != plaintext, "Plaintext if null!")
    encryptRaw(plaintext.getBytes(UTF_8))
  }
  
  /** Encrypt Bytes returning the tuple: (iv, ciphertext) */
  def encryptRaw(plaintext: Array[Byte]): (Array[Byte], Array[Byte]) = {
    val iv: Array[Byte] = new Array[Byte](cipher.getBlockSize)
    secureRandom.nextBytes(iv)
    val ciphertext: Array[Byte] = doCipher(ENCRYPT, iv, plaintext)
    (iv, ciphertext)
  }
  
  /** Decrypt a string encrypting using encryptBase64String() */
  def decryptBase64String(base64IvAndCiphertext: String): String = {
    require(null != base64IvAndCiphertext, "Null base64IvAndCiphertext parameter")
    val plaintextBytes: Array[Byte] = decrypt(Base64.decode(base64IvAndCiphertext))
    new String(plaintextBytes, UTF_8)
  }
  
  /** Decrypt given the combined IV and Ciphertext */
  def decrypt(ivAndCiphertext: Array[Byte]): Array[Byte] = {
    val iv: Array[Byte] = java.util.Arrays.copyOfRange(ivAndCiphertext, 0, cipher.getBlockSize)
    val ciphertext: Array[Byte] = java.util.Arrays.copyOfRange(ivAndCiphertext, cipher.getBlockSize, ivAndCiphertext.length)
    doCipher(DECRYPT, iv, ciphertext)
  }
  
  /** Decrypt given the IV and Ciphertext */
  def decrypt(iv: Array[Byte], ciphertext: Array[Byte]): Array[Byte] = doCipher(DECRYPT, iv, ciphertext)
  
  def macBase64(data: String): String = macBase64(data.getBytes(UTF_8))
  def macBase64URLSafe(data: String): String = macBase64(data.getBytes(UTF_8))
  def macBase64(data: String, urlSafe: Boolean): String = macBase64(data.getBytes(UTF_8), urlSafe = urlSafe)
  
  /** The Base64 Encoded MAC for an array of bytes */
  def macBase64(data: Array[Byte]): String = macBase64(data, urlSafe = false)
  
  def macBase64URLSafe(data: Array[Byte]): String = macBase64(data, urlSafe = true)
  
  def macBase64(data: Array[Byte], urlSafe: Boolean): String = Base64.encodeBytes(mac(data), if (urlSafe) Base64.URL_SAFE else Base64.NO_OPTIONS)
  
  /** The Hex Encoded MAC for a String */
  def macHex(data: String): String = macHex(data.getBytes(UTF_8))
  
  /** The Hex Encoded MAC for an array of bytes */
  def macHex(data: Array[Byte]): String = new String(Hex.encodeHex(mac(data)))
   
  /** Calculate the MAC for an array of bytes */
  def mac(data: Array[Byte]): Array[Byte] = mac.synchronized {
    mac.init(new KeyParameter(key))
    mac.update(data, 0, data.length)
    val out: Array[Byte] = new Array[Byte](mac.getMacSize)
    mac.doFinal(out, 0)
    out
  }
  
  private def sha256(data: Array[Byte]): Array[Byte] = {
    // Note: doDigest was manually inlined here since proguard complains and then scala (at least the console) also complains
    val d: Digest = new SHA256Digest
    d.update(data, 0, data.length)
    val buf = new Array[Byte](d.getDigestSize)
    d.doFinal(buf, 0)
    buf
  }

  private def doCipher(doEncrypt: Boolean, iv: Array[Byte], data: Array[Byte]): Array[Byte] = cipher.synchronized{
    val keyWithIV: ParametersWithIV = new ParametersWithIV(new KeyParameter(keyBytes), iv)
    cipher.init(doEncrypt, keyWithIV)
    val estimatedSize: Int = cipher.getOutputSize(data.length)
    val outBytes: Array[Byte] = new Array[Byte](estimatedSize)
    var outLen: Int = cipher.processBytes(data, 0, data.length, outBytes, 0)
    outLen += cipher.doFinal(outBytes, outLen)
    if(outLen < estimatedSize) {
      val tmp: Array[Byte] = new Array[Byte](outLen)
      System.arraycopy(outBytes, 0, tmp, 0, outLen)
      tmp
    } else outBytes
  }
  
  // Commenting out and inlining manually since proguard complains and the scala console doesn't like this
//  private def doDigest(d: Digest, data: Array[Byte]): Array[Byte] = {
//    d.update(data, 0, data.length)
//    val buf = new Array[Byte](d.getDigestSize)
//    d.doFinal(buf, 0)
//    buf
//  }
}