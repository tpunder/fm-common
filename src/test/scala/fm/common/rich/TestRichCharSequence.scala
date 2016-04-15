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

import org.scalatest.FunSuite
import org.scalatest.Matchers

final class TestRichCharSequence extends FunSuite with Matchers {
  import fm.common.Implicits._
  
  test("isBlank null") {
    null.asInstanceOf[String].isBlank should equal(true)
  }
  
  test("isBlank whitespace") {
    "".isBlank should equal(true)
    " ".isBlank should equal(true)
    "  ".isBlank should equal(true)
    "\t".isBlank should equal(true)
    "\n".isBlank should equal(true)
    "\r".isBlank should equal(true)
  }
  
  test("isBlank whitespace followed by a character") {
    "a".isBlank should equal(false)
    " a".isBlank should equal(false)
    "  a".isBlank should equal(false)
    "\ta".isBlank should equal(false)
    "\na".isBlank should equal(false)
    "\ra".isBlank should equal(false)
  }
  
  test("isBlank non-empty") {
    "abc".isBlank should equal(false)
    "123".isBlank should equal(false)
    "_".isBlank should equal(false)
    "!".isBlank should equal(false)
    "@".isBlank should equal(false)
    "#".isBlank should equal(false)
  }
  
  test("nextCharsMatch") {
    ",".nextCharsMatch(",", 0) should equal(true)
    ", ".nextCharsMatch(",", 0) should equal(true)

    "".nextCharsMatch(",", 0) should equal(false)
    " ,".nextCharsMatch(",", 0) should equal(false)
    
    "|-|".nextCharsMatch("|-|", 0) should equal(true)
    "|-|asd".nextCharsMatch("|-|", 0) should equal(true)
    
    "".nextCharsMatch("|-|", 0) should equal(false)
    "|".nextCharsMatch("|-|", 0) should equal(false)
    "|-".nextCharsMatch("|-|", 0) should equal(false)
    "|--".nextCharsMatch("|-|", 0) should equal(false)

    " ,".nextCharsMatch(",", 1) should equal(true)
    " a".nextCharsMatch(",", 1) should equal(false)
    " a,".nextCharsMatch(",", 2) should equal(true)

    "".nextCharsMatch("asd", 0) should equal(false)
    "a".nextCharsMatch("asd", 0) should equal(false)
    "as".nextCharsMatch("asd", 0) should equal(false)
    "asf".nextCharsMatch("asd", 0) should equal(false)

    "".nextCharsMatch("", 0) should equal(false)
    "foobar".nextCharsMatch("", 0) should equal(false)

    "".nextCharsMatch(null, 0) should equal(false)
    "foobar".nextCharsMatch(null, 0) should equal(false)
  }
  
  test("indexesOf") {
    "aaaaaaaa".indexesOf("aa", withOverlaps = false) should equal (List(0, 2, 4, 6))
    "aaaaaaaa".indexesOf(target = "aa", withOverlaps = true) should equal (List(0, 1, 2, 3, 4, 5, 6))
    "aaaaaaaa".indexesOf("b", withOverlaps = false) should equal (Nil)
  }

}