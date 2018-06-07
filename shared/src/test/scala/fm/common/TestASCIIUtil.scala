/*
 * Copyright 2018 Frugal Mechanic (http://frugalmechanic.com)
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

final class TestASCIIUtil extends FunSuite with Matchers {

  private val ascii: String = (0 to 127).map{ _.toChar }.mkString

  test("toASCIIChar - 0-127 ascii") {
    ASCIIUtil.toASCIIChar('a') should equal ('a')
    ASCIIUtil.toASCIIChar('*') should equal ('*')

    ascii.foreach { ch: Char => ASCIIUtil.toASCIIChar(ch) should equal (ch) }
  }

  test("toASCIIChar - accents") {
    ASCIIUtil.toASCIIChar('\u204E') should equal ('*')
    ASCIIUtil.toASCIIChar('\u2052') should equal ('%')
  }

  test("convertToASCII - ascii") {
    ASCIIUtil.convertToASCII(ascii) shouldBe theSameInstanceAs (ascii)
    ASCIIUtil.convertToASCII("foobar") shouldBe theSameInstanceAs ("foobar")
    ASCIIUtil.convertToASCII("!@#$%^&*()_+") shouldBe theSameInstanceAs ("!@#$%^&*()_+")
  }

  test("convertToASCII - non-ascii") {
    ASCIIUtil.convertToASCII("\u204E") should equal ("*")
    ASCIIUtil.convertToASCII("\u204E"+ascii) should equal ("*"+ascii)
    ASCIIUtil.convertToASCII("\u2052") should equal ("%")
    ASCIIUtil.convertToASCII("Æ") should equal ("AE")
    ASCIIUtil.convertToASCII(ascii+"Æ") should equal (ascii+"AE")
    ASCIIUtil.convertToASCII("Æ"+ascii) should equal ("AE"+ascii)
    ASCIIUtil.convertToASCII(ascii+"Foo Bar \u204E \u2052 Æ") should equal (ascii+"Foo Bar * % AE")
  }
}
