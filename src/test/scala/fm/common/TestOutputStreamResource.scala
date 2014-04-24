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
    
    InputStreamResource.wrap(bis, fileName = name).readToString("UTF-8") should equal ("Hello World!\n")
  }
}