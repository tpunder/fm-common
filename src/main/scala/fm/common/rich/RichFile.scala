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

import java.io.File
import scala.collection.mutable.Builder

final class RichFile(val f: File) extends AnyVal {

  /**
   * The extension (if any) of this file
   */
  def extension: Option[String] = {
    require(f.isFile, s"Not a file: $f")
    val name: String = f.getName()
    val indexOfDot: Int = name.lastIndexOf('.')
    if (-1 == indexOfDot) None else Some(name.substring(indexOfDot+1))
  }
  
  /**
   * The name of the file without it's extension
   */
  def nameWithoutExtension: String = {
    require(f.isFile, s"Not a file: $f")
    val name: String = f.getName()
    val indexOfDot: Int = name.lastIndexOf('.')
    if (-1 == indexOfDot) name else name.substring(0, indexOfDot)
  }
  
  /**
   * Find all files under this directory (directories are not included in the result)
   */
  def findFiles(recursive: Boolean = true): Vector[File]= {
    val builder: Builder[File, Vector[File]] = Vector.newBuilder[File]
    findFiles0(f, recursive, builder)
    builder.result
  }
  
  private def findFiles0(dir: File, recursive: Boolean, builder: Builder[File, Vector[File]]): Unit = {
    require(dir.isDirectory, s"Not a directory: $dir")
    
    val children: Array[File] = dir.listFiles()
    
    if (null != children) children.foreach { child: File =>
      if (child.isDirectory && recursive) findFiles0(child, recursive, builder)
      else if (child.isFile) builder += child
    }
  }
}