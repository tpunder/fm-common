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

import org.scalatest.FunSuite
import org.scalatest.Matchers
import java.io.{ByteArrayInputStream}

final class TestOutputStreamResource extends FunSuite with Matchers {
//  test(".tar.gz") { check("hello_world.txt.tar.gz") }
//  test(".tgz")    { check("hello_world.txt.tgz") }
//  test(".tar.bz")    { check("hello_world.txt.tar.bz") }
//  test(".tar.bz2")   { check("hello_world.txt.tar.bz2") }
//  test(".tar.bzip2") { check("hello_world.txt.tar.bzip2") }
//  test(".tbz2")   { check("hello_world.txt.tbz2") }
//  test(".tbz")    { check("hello_world.txt.tbz") }
//  test(".tar.xz")    { check("hello_world.txt.tar.xz") }
//  test(".tar")    { check("hello_world.txt.tar") }
  test(".gz")     { check("hello_world.txt.gz") }
  test(".bzip2")  { check("hello_world.txt.bzip2") }
  test(".bz2")    { check("hello_world.txt.bz2") }
  test(".bz")     { check("hello_world.txt.bz") }
  test(".snappy") { check("hello_world.txt.snappy") }
  test(".xz")     { check("hello_world.txt.xz") }
  test(".zip")    { check("hello_world.txt.zip") }
  test(".jar")    { check("hello_world.txt.jar") }
 
  private def check(name: String): Unit = {
    val bos = new ByteArrayOutputStream
    OutputStreamResource.wrap(bos, fileName = name).writer("UTF-8").use { _.write("Hello World!\n") }
    
    val bis = new ByteArrayInputStream(bos.toByteArray())
    
    InputStreamResource.forInputStream(bis, fileName = name).readToString("UTF-8") should equal ("Hello World!\n")
  }
}