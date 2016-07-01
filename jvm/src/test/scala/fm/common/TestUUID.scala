/*
 * Copyright 2015 Frugal Mechanic (http://frugalmechanic.com)
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

final class TestUUID extends FunSuite with Matchers {
  test("Encoding/Decoding") {
    check(0, 0, 0, 0)
    check(1, 1, 1, 1)
    check(1, 2, 3, 4)
    check(1, 1, -1, 1)
    
    check(0, 0, Short.MinValue, 0)
    check(0xffffffffffffL, Short.MaxValue, Short.MaxValue, 0xffffffffffffL)
    
    check(123456789L, 30321, 12345, 987654321L)
    check(123456789L, 30321, -12345, 987654321L)
  }
  
  test("Invalid Timestamp Values") {
    checkInvalidArgument(-1, 123, 456, 789)
    checkInvalidArgument(Long.MinValue, 123, 456, 789)
    checkInvalidArgument(Long.MaxValue, 123, 456, 789)
    checkInvalidArgument(0xffffffffffffL + 1, 123, 456, 789)
  }
  
  test("Invalid Counter Values") {
    checkInvalidArgument(123, -1, 456, 789)
    checkInvalidArgument(123, Int.MinValue, 456, 789)
    checkInvalidArgument(123, Int.MinValue, 456, 789)
    checkInvalidArgument(123, 65536, 456, 789)
  }
  
  test("Invalid NodeId Values") {
    checkInvalidArgument(123, 456, Short.MinValue - 1, 789)
    checkInvalidArgument(123, 456, Short.MaxValue + 1, 789)
    checkInvalidArgument(123, 456, Int.MinValue, 789)
    checkInvalidArgument(123, 456, Int.MaxValue, 789)
  }
  
  test("Invalid Random Values") {
    checkInvalidArgument(123, 456, 789, -1)
    checkInvalidArgument(123, 456, 789, 0xffffffffffffL + 1)
    checkInvalidArgument(123, 456, 789, Long.MinValue)
    checkInvalidArgument(123, 456, 789, Long.MaxValue)
  }
  
  test("Random Values") {
    (0 until 1000000).foreach{ i => checkRandom() }
  }
  
  private def checkRandom(): Unit = {
    val uuid: UUID = UUID()
    check(uuid.epochMilli, uuid.counter, uuid.nodeId, uuid.random, uuid)
  }
  
  private def check(epochMilli: Long, counter: Int, nodeId: Int, random: Long): Unit = TestHelpers.withCallerInfo {
    val uuid: UUID = UUID(epochMilli, counter, nodeId, random)
    check(epochMilli, counter, nodeId, random, uuid)
    check(epochMilli, counter, nodeId, random, UUID(uuid.toHex))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toBase64))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toBase64NoPadding))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toBase64URL))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toBase64URLNoPadding))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toPrettyString))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toPrettyString('_')))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toPrettyString(':')))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toPrettyString('?')))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toStandardString))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toStandardString('_')))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toStandardString(':')))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toStandardString('?')))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toString))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toJavaUUID))
    check(epochMilli, counter, nodeId, random, UUID(uuid.toJavaUUID.toString))
  }
  
  private def check(epochMilli: Long, counter: Int, nodeId: Int, random: Long, uuid: UUID): Unit = TestHelpers.withCallerInfo {
    withClue ("epochMilli:") { uuid.epochMilli should equal (epochMilli) }
    withClue ("counter:") { uuid.counter should equal (counter) }
    withClue ("nodeId:") { uuid.nodeId should equal (nodeId) }
    withClue ("random:") { uuid.random should equal (random) }
  }
  
  private def checkInvalidArgument(epochMilli: Long, counter: Int, nodeId: Int, random: Long): Unit = TestHelpers.withCallerInfo{
    an [IllegalArgumentException] should be thrownBy { check(epochMilli, counter, nodeId, random) }
  }
}
