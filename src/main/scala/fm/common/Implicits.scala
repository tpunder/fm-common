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

import fm.common.rich._

import java.io.InputStream
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import java.math.{BigDecimal => JavaBigDecimal, BigInteger => JavaBigInteger}
import scala.concurrent.Future
import scala.math.{BigDecimal => ScalaBigDecimal, BigInt => ScalaBigInt}

object Implicits extends Implicits

trait Implicits {  
  implicit def toRichCharSequence(s: CharSequence): RichCharSequence = new RichCharSequence(s)
  implicit def toRichString(s: String): RichString = new RichString(s)
  
  implicit def toRichTraversableOnce[T](t: scala.collection.TraversableOnce[T]) = new RichTraversableOnce(t)
  
  implicit def toRichMap[A,B,This <: scala.collection.MapLike[A,B,This] with scala.collection.Map[A,B]](m: scala.collection.MapLike[A,B,This]) = new RichMap(m)
  implicit def toRichConcurrentMap[K,V](m: java.util.concurrent.ConcurrentMap[K,V]): RichConcurrentMap[K,V] = new RichConcurrentMap(m)
  
  implicit def toRichFuture[V](f: Future[V]): RichFuture[V] = RichFuture(f)
  
  implicit def toRichURI(uri: URI): RichURI = new RichURI(uri)
  implicit def toRichURI(url: URL): RichURL = new RichURL(url)
  
  implicit def toRichAtomicInteger(int: AtomicInteger): RichAtomicInteger =  new RichAtomicInteger(int)
  implicit def toRichAtomicLong(long: AtomicLong): RichAtomicLong =  new RichAtomicLong(long)
  
  implicit def bigIntegerOrdering: Ordering[JavaBigInteger] = RichBigInteger
  implicit def toRichBigInteger(i: JavaBigInteger): RichBigInteger = new RichBigInteger(i)
  implicit def toRichBigInteger(i: ScalaBigInt): RichBigInteger = new RichBigInteger(i.bigInteger)
  
  implicit def bigDecimalOrdering: Ordering[JavaBigDecimal] = RichBigDecimal
  implicit def toRichBigDecimal(d: JavaBigDecimal): RichBigDecimal = new RichBigDecimal(d)
  implicit def toRichBigDecimal(d: ScalaBigDecimal): RichBigDecimal = new RichBigDecimal(d.bigDecimal)
  
  implicit def toRichInputStream(is: InputStream): RichInputStream = new RichInputStream(is)
}