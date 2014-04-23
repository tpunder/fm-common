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

import java.io._
import java.nio.charset.Charset

object FileUtil extends Logging {
  private val readBufferSize = 8192
  private val defaultCharset = Charset.forName("UTF-8")
  
  def touch(f: File): Unit = if (!f.exists) f.createNewFile

  def md5(f: File): Array[Byte] = Resource.using(new FileInputStream(f)){ IOUtils.md5(_) }
  def md5Hex(f: File): String = Resource.using(new FileInputStream(f)){ IOUtils.md5Hex(_) }
  def sha1(f: File): Array[Byte] = Resource.using(new FileInputStream(f)){ IOUtils.sha1(_) }
  def sha1Hex(f: File): String = Resource.using(new FileInputStream(f)){ IOUtils.sha1Hex(_) }

  def detectCharset(f: File): Option[Charset] = InputStreamResource.forFileOrResource(f).detectCharset()
  def detectCharsetName(f: File): Option[String] = InputStreamResource.forFileOrResource(f).detectCharsetName()

  def getDirectoryForFile(f: File): File = {
    val tmp = if(f.isDirectory) f else f.getParentFile
    assert(tmp.isDirectory)
    tmp
  }

  def writeRawFile(f: File, overwrite: Boolean)(fun: OutputStream => Unit): Unit = FileOutputStreamResource(f, overwrite = overwrite, autoCompress = false).use(fun)
  
  def writeRawFile(f: File, is: InputStream, overwrite: Boolean): Unit = writeRawFile(f, overwrite){ os =>
    IOUtils.copy(is, os)
  }
  
  def writeFile[T](f: File, overwrite: Boolean)(fun: OutputStream => T): T = FileOutputStreamResource(f, overwrite = overwrite).use(fun)

  def writeFile(f: File, contents: String, overwrite: Boolean): Unit = writeFile(f, contents.getBytes("UTF-8"), overwrite)
  
  def writeFile(f: File, bytes: Array[Byte], overwrite: Boolean): Unit = {
    writeFile(f, overwrite) { os =>
      os.write(bytes)
    }
  }
  
  def writeFile(f: File, is: InputStream, overwrite: Boolean): Unit = writeFile(f, overwrite){ os =>
    IOUtils.copy(is, os)
  }
  
  def copy(src: File, dst: File, overwrite: Boolean = true) {    
    def bothEndWith(s: String): Boolean = src.getName.endsWith(s) && dst.getName.endsWith(s)
    
    // Only enable autoDecompress/autoCompress if the compression formats don't already match 
    val compression = if(bothEndWith(".gz") || bothEndWith(".zip") || bothEndWith(".snappy")) false else true
    
    InputStreamResource.forFile(src, autoDecompress = compression).use { is =>
      FileOutputStreamResource(dst, autoCompress = compression).use { os =>
        IOUtils.copy(is, os)
      }
    }
  }
  
  def readFile(file: String): String = readFile(file, "UTF-8")
  
  def readFile(file: String, encoding: String): String = readFile(new File(file), encoding)

  def readFile(f: File): String = readFile(f, "UTF-8")
  
  def readFile(f: File, encoding: String): String = InputStreamResource.forFile(f).readToString(encoding)

  def readLines(file: File)(f: String => Unit): Unit = readLines(InputStreamResource.forFile(file).bufferedReader())(f)
  
  def readLines(is: InputStream)(f: String => Unit): Unit = readLines(InputStreamResource.wrap(is).bufferedReader())(f)
  
  def readLines(resource: Resource[BufferedReader])(f: String => Unit): Unit = resource.use{ reader: BufferedReader =>
    var line = reader.readLine
    while(null != line) {
      f(line)
      line = reader.readLine
    }
  }

  def readBytes(file: String): Array[Byte] = readBytes(new File(file))

  def readBytes(f: File): Array[Byte] = InputStreamResource.forFile(f).readBytes()

  def readInputStream(is: InputStream): String = readInputStream(is, "UTF-8")
  
  def readInputStream(is: InputStream, encoding: String): String = readInputStream(is, Charset.forName(encoding))
  
  def readInputStream(is: InputStream, charset: Charset): String = {
    val reader = new BufferedReader(new InputStreamReader(is, charset))
    val writer = new StringWriter()

    IOUtils.copy(reader, writer)
    reader.close
    writer.toString
  }

  def rm_rf(dir: File, keepDirectory: Boolean = false): Boolean = {
    logger.warn("rm -rf " + dir.getAbsolutePath)
    if(dir.isDirectory) {
      val children = dir.list
      children.foreach { child =>
        if(!rm_rf(new File(dir, child))) return false
      }

      if(!keepDirectory) dir.delete() else true
    } else {
      val success = dir.delete()
      if(!success) logger.warn(s"File deletion for ${dir.getAbsolutePath} failed")
      success
    }
  }

}

