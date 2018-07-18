/*
 * Copyright 2016 Frugal Mechanic (http://frugalmechanic.com)
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

import org.scalatest.{FunSuite, Matchers}

// Note: Using ≡ and ≠ to avoid conflicts with ScalaTest === method
final class TestTypeSafeEquals extends FunSuite with Matchers {

  test("Basics") {
    1 ≡ 1 should equal (true)
    1 ≠ 1 should equal (false)

    "foo" ≡ "bar" should equal (false)
    "foo" ≠ "bar" should equal (true)
    
    "1d ≡ 1" shouldNot compile
    "1 ≡ 1d" shouldNot compile
    
    "1 ≡ Option(1)" shouldNot compile
    "Option(1) ≡ 1" shouldNot compile
    
    """"foo" ≡ Option("foo")""" shouldNot compile
    """Option("foo") ≡ "foo""" shouldNot compile
  }

  test("nulls") {
    val nullStr: String = null
    val nonNullStr: String = "non-null"

    nullStr ≡ null should equal (true)
    nullStr ≠ null should equal (false)

    nonNullStr ≡ null should equal (false)
    nonNullStr ≠ null should equal (true)

    // Can't get the implicits to work for these to compile:
    
//    null ≡ nullStr should equal (true)
//    null ≠ nullStr should equal (false)

//    null ≡ null should equal (true)
//    null ≠ null should equal (false)

    """null ≡ 1""" shouldNot compile
    """null ≠ 1""" shouldNot compile
    """1 ≡ null""" shouldNot compile
    """1 ≠ null""" shouldNot compile
  }
  
  test("Subtypes") {
    """1 ≡ Foo("foo")""" shouldNot compile
    """Foo("foo") ≡ 1""" shouldNot compile
    
    Foo("foo") ≡ Foo("bar") should equal (false)
    Foo("foo") ≠ Foo("bar") should equal (true)
    
    val fooAsBase: Base = Foo("foo")
    val foo: Foo = Foo("foo")
    
    val barAsBase: Base = Bar(123)
    val bar: Bar = Bar(123)
    
    fooAsBase ≡ foo should equal (true)
    foo ≡ fooAsBase should equal (true)
    
    fooAsBase ≠ barAsBase should equal (true)
    fooAsBase ≠ bar should equal (true)
    
    "foo ≡ bar" shouldNot compile
    "bar ≡ foo" shouldNot compile
  }
  
  sealed trait Base
  case class Foo(foo: String) extends Base
  case class Bar(bar: Int) extends Base
}
