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
package fm.common.rich

import org.scalatest.{FunSuite,Matchers}
import fm.common.Implicits._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class TestRichTraversableOnce extends FunSuite with Matchers {
  
  test("mkStringOrBlank") {
    Vector().mkStringOrBlank("[start]", "[sep]", "[end]") should equal ("")
    Vector("foo").mkStringOrBlank("[start]", "[sep]", "[end]") should equal ("[start]foo[end]")
    Vector("foo", "bar").mkStringOrBlank("[start]", "[sep]", "[end]") should equal ("[start]foo[sep]bar[end]")
  }
  
  test("countBy") {
    Vector(1,2,2,3,3,3,4,4,4,4).countBy{ i => i} should equal (Map(1 -> 1, 2 -> 2, 3 -> 3, 4 -> 4))
  }
  
  test("collapseBy") {
     Vector(2,4,6,1,3,2,5,7).collapseBy{ _ % 2 == 0 } should equal (Vector((true,Vector(2, 4, 6)), (false,Vector(1, 3)), (true,Vector(2)), (false,Vector(5, 7))))
  }
  
  test("findMapped") {
    Vector(1,2,3,4).findMapped{ i: Int => if (i % 2 == 0) Some("foo") else None } should equal (Some("foo"))
    Vector(1,2,3,4).findMapped{ i: Int => if (i == 123) Some("foo") else None } should equal (None)
  }
  
  test("findMappedFuture") {
    Await.result(Vector(1,2,3,4).findMappedFuture{ i: Int => if (i == 2) Future.successful(Some("foo")) else Future.successful(None) }, Duration.Inf) should equal (Some("foo"))
    Await.result(Vector(1,2,3).findMappedFuture{ i: Int => if (i == 123) Future.successful(Some("foo")) else Future.successful(None) }, Duration.Inf) should equal (None)
    
    // Only the Future for the first element should be run.
    Await.result(Vector(1,2,3,4).findMappedFuture{ i: Int => if (i == 1) Future.successful(Some("foo")) else { System.exit(-1); ??? } }, Duration.Inf) should equal (Some("foo"))
  }
}
