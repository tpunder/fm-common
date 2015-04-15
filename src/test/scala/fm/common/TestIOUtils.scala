package fm.common

import org.scalatest.FunSuite
import org.scalatest.Matchers
import java.io.{BufferedInputStream, File}
import java.nio.charset.Charset

final class TestIOUtils extends FunSuite with Matchers {
  // Test detecting charset encoding
  test("detectCharset - UTF-8 with BOM")    { checkDetectCharset("quickbrown-UTF-8-with-BOM.txt", "UTF-8") }
  test("detectCharset - UTF-8 no BOM")      { checkDetectCharset("quickbrown-UTF-8-no-BOM.txt", "UTF-8") }
  
  test("detectCharset - UTF-16BE with BOM") { checkDetectCharset("quickbrown-UTF-16BE-with-BOM.txt", "UTF-16BE") }
  //test("detectCharset - UTF-16BE no BOM")   { checkDetectCharset("quickbrown-UTF-16BE-no-BOM.txt", "UTF-16BE") }
  
  test("detectCharset - UTF-16LE with BOM") { checkDetectCharset("quickbrown-UTF-16LE-with-BOM.txt", "UTF-16LE") }
  //test("detectCharset - UTF-16LE no BOM")   { checkDetectCharset("quickbrown-UTF-16LE-no-BOM.txt", "UTF-16LE") }
  
  test("detectCharset - UTF-32BE with BOM") { checkDetectCharset("quickbrown-UTF-32BE-with-BOM.txt", "UTF-32BE") }
  //test("detectCharset - UTF-32BE no BOM")   { checkDetectCharset("quickbrown-UTF-32BE-no-BOM.txt", "UTF-32BE") }
  
  test("detectCharset - UTF-32LE with BOM") { checkDetectCharset("quickbrown-UTF-32LE-with-BOM.txt", "UTF-32LE") }
  //test("detectCharset - UTF-32LE no BOM")   { checkDetectCharset("quickbrown-UTF-32LE-no-BOM.txt", "UTF-32LE") }
  
  test("detectCharset - Windows-1252")      { checkDetectCharset("quickbrown-modified-Windows-1252.txt", "Windows-1252") }
  
  private def checkDetectCharset(file: String, charsetName: String): Unit = {
    InputStreamResource.forResource(new File(s"encoding/$file")).flatMap { _.toBufferedInputStream }.foreach { bis: BufferedInputStream =>
      IOUtils.detectCharset(bis, true) should equal (Some(Charset.forName(charsetName)))
    }
  } 
}

