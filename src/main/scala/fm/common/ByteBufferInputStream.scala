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

import java.io.InputStream
import java.nio.ByteBuffer

/**
 * A Simple InputStream wrapper around a ByteBuffer
 */
final class ByteBufferInputStream(buf: ByteBuffer) extends InputStream {
  def read(): Int = {
    if (!buf.hasRemaining()) return -1
    buf.get() & 0xFF
  }
  
  override def read(bytes: Array[Byte], off: Int, len: Int): Int = {
    if (!buf.hasRemaining()) return -1
    
    val lenToRead = math.min(len, buf.remaining())
    buf.get(bytes, off, lenToRead)
    lenToRead
  }
}
