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
import java.io.{File, InputStream}
import java.nio.file.Path
import java.math.{BigDecimal => JavaBigDecimal, BigInteger => JavaBigInteger}
import java.util.Locale
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import scala.concurrent.{Await, Future}
import scala.math.{BigDecimal => ScalaBigDecimal, BigInt => ScalaBigInt}
import scala.util.Try

object Implicits extends Implicits {
  implicit class ToImmutableArrayByte   (val col: TraversableOnce[Byte])    extends AnyVal { def toImmutableArray: ImmutableArray[Byte]    = ImmutableArray.copy(col) }
  implicit class ToImmutableArrayShort  (val col: TraversableOnce[Short])   extends AnyVal { def toImmutableArray: ImmutableArray[Short]   = ImmutableArray.copy(col) }
  implicit class ToImmutableArrayInt    (val col: TraversableOnce[Int])     extends AnyVal { def toImmutableArray: ImmutableArray[Int]     = ImmutableArray.copy(col) }
  implicit class ToImmutableArrayLong   (val col: TraversableOnce[Long])    extends AnyVal { def toImmutableArray: ImmutableArray[Long]    = ImmutableArray.copy(col) }
  implicit class ToImmutableArrayFloat  (val col: TraversableOnce[Float])   extends AnyVal { def toImmutableArray: ImmutableArray[Float]   = ImmutableArray.copy(col) }
  implicit class ToImmutableArrayDouble (val col: TraversableOnce[Double])  extends AnyVal { def toImmutableArray: ImmutableArray[Double]  = ImmutableArray.copy(col) }
  implicit class ToImmutableArrayBoolean(val col: TraversableOnce[Boolean]) extends AnyVal { def toImmutableArray: ImmutableArray[Boolean] = ImmutableArray.copy(col) }
  implicit class ToImmutableArrayChar   (val col: TraversableOnce[Char])    extends AnyVal { def toImmutableArray: ImmutableArray[Char]    = ImmutableArray.copy(col) }
  
  implicit class ToImmutableArrayAnyRef[T <: AnyRef](val col: TraversableOnce[T]) extends AnyVal { def toImmutableArray: ImmutableArray[T] = ImmutableArray.copy[AnyRef](col).asInstanceOf[ImmutableArray[T]] }
}

trait Implicits extends OrderingImplicits {  
  implicit def toRichCharSequence(s: CharSequence): RichCharSequence = new RichCharSequence(s)
  implicit def toRichString(s: String): RichString = new RichString(s)
  implicit def toRichStringOption(opt: Option[String]): RichStringOption = new RichStringOption(opt)
  
  implicit def toRichTraversableOnce[T](t: scala.collection.TraversableOnce[T]) = new RichTraversableOnce(t)
  
  implicit def toRichOption[T](opt: Option[T]): RichOption[T] = new RichOption[T](opt)
  
  implicit def toRichMap[A,B,This <: scala.collection.MapLike[A,B,This] with scala.collection.Map[A,B]](m: scala.collection.MapLike[A,B,This]) = new RichMap(m)
  implicit def toRichConcurrentMap[K,V](m: java.util.concurrent.ConcurrentMap[K,V]): RichConcurrentMap[K,V] = new RichConcurrentMap(m)
  
  implicit def toRichTry[T](t: Try[T]): RichTry[T] = new RichTry(t)
  implicit def toRichFuture[V](f: Future[V]): RichFuture[V] = RichFuture(f)
  implicit def toRichAwait[V](await: Await.type): RichAwait = new RichAwait(await)
  
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
  
  implicit def toRichFile(f: File): RichFile = new RichFile(f)
  implicit def toRichPath(p: Path): RichPath = new RichPath(p)
  implicit def toRichInputStream(is: InputStream): RichInputStream = new RichInputStream(is)
  
  implicit def toRichLocale(locale: Locale): RichLocale = new RichLocale(locale)
}