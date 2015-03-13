/*
 * Copyright 2015 Frugal Mechanic (http://frugalmechanic.com)
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

import javax.imageio.stream.ImageInputStreamImpl

/**
 * An ImageInputStream implementation that reads from an Array[Byte]
 */
final class ByteArrayImageInputStream(val bytes: Array[Byte], val bytesOffset: Int, val bytesLength: Int) extends ImageInputStreamImpl {
  def this(bytes: Array[Byte]) = this(bytes, 0, bytes.length)
  
  //
  // DUPLICATED IN ByteArrayImageOutputStream
  //
  final def read(): Int = if (streamPos >= bytesLength) -1 else {
    if (bitOffset > 0) bitOffset = 0
    val res: Int = bytes(streamPos.toInt + bytesOffset) & 0xff
    streamPos += 1
    res
  }
  
  //
  // DUPLICATED IN ByteArrayImageOutputStream
  //
  final def read(b: Array[Byte], off: Int, len: Int): Int = if (streamPos >= bytesLength) -1 else {
    if (bitOffset > 0) bitOffset = 0
    val lengthRead: Int = math.min(len, bytesLength - streamPos.toInt)
    System.arraycopy(bytes, streamPos.toInt + bytesOffset, b, off, lengthRead)
    streamPos += lengthRead
    lengthRead
  }
  
  final override def isCachedMemory(): Boolean = true
  
  override def length(): Long = bytesLength
}