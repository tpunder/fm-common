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

import scala.annotation.unchecked.uncheckedVariance
import scala.reflect.ClassTag
import scala.collection.IndexedSeqOptimized
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.IndexedSeq
import scala.collection.mutable.Builder

object ImmutableArray {
  def apply[@specialized A: ClassTag](elems: A*): ImmutableArray[A] = {
    if (elems.isEmpty) empty[A]
    else copy[A](elems)
  }  
  
  /**
   * Create a new ImmutableArray by creating a copy of the passed in collection
   */
  def copy[@specialized A: ClassTag](col: TraversableOnce[A]): ImmutableArray[A] = copy[A](col.toArray[A])
  
  /**
   * Create a new Immutable Array by creating a copy of the passed in array
   */
  def copy[@specialized A: ClassTag](arr: Array[A]): ImmutableArray[A] = {
    if (arr.length == 0) empty else {
      val dst = new Array[A](arr.length)
      System.arraycopy(arr, 0, dst, 0, arr.length)
      new ImmutableArray[A](dst)
    }
  }
  
  /**
   * Wrap an existing array in an ImmutableArray.  The passed in array must not be changed after calling this!
   */
  def wrap[@specialized A: ClassTag](arr: Array[A]): ImmutableArray[A] = {
    if (arr.length == 0) empty else new ImmutableArray(arr)
  }

  private type Coll = ImmutableArray[_]
  
  implicit val canBuildFromChar: CanBuildFrom[Coll, Char, ImmutableArray[Char]] = new CBF(builderForChar)
  implicit val canBuildFromShort: CanBuildFrom[Coll, Short, ImmutableArray[Short]] = new CBF(builderForShort)
  implicit val canBuildFromFloat: CanBuildFrom[Coll, Float, ImmutableArray[Float]] = new CBF(builderForFloat)
  implicit val canBuildFromDouble: CanBuildFrom[Coll, Double, ImmutableArray[Double]] = new CBF(builderForDouble)
  implicit val canBuildFromInt: CanBuildFrom[Coll, Int, ImmutableArray[Int]] = new CBF(builderForInt)
  implicit val canBuildFromLong: CanBuildFrom[Coll, Long, ImmutableArray[Long]] = new CBF(builderForLong)
  
  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, ImmutableArray[A]] = new CBF[A](builderForAnyRef.asInstanceOf[ImmutableArrayBuilder[A]])
  
  private class CBF[Elem](makeBuilder: => ImmutableArrayBuilder[Elem]) extends CanBuildFrom[Coll, Elem, ImmutableArray[Elem]] {
    def apply(): ImmutableArrayBuilder[Elem] = makeBuilder
    def apply(from: Coll): ImmutableArrayBuilder[Elem] = makeBuilder
  }
  
  def empty[A]: ImmutableArray[A] = _empty.asInstanceOf[ImmutableArray[A]]
  
  def newBuilder[@specialized A: ClassTag]: ImmutableArrayBuilder[A] = new ImmutableArrayBuilder[A](0)
  def newBuilder[@specialized A: ClassTag](initialSize: Int): ImmutableArrayBuilder[A] = new ImmutableArrayBuilder[A](initialSize)
  
  def builderForChar: ImmutableArrayBuilder[Char] = new ImmutableArrayBuilder[Char](0)
  def builderForShort: ImmutableArrayBuilder[Short] = new ImmutableArrayBuilder[Short](0)
  def builderForFloat: ImmutableArrayBuilder[Float] = new ImmutableArrayBuilder[Float](0)
  def builderForDouble: ImmutableArrayBuilder[Double] = new ImmutableArrayBuilder[Double](0)
  def builderForInt: ImmutableArrayBuilder[Int] = new ImmutableArrayBuilder[Int](0)
  def builderForLong: ImmutableArrayBuilder[Long] = new ImmutableArrayBuilder[Long](0)
  def builderForAnyRef: ImmutableArrayBuilder[AnyRef] = new ImmutableArrayBuilder[AnyRef](0)
  
  private val _empty: ImmutableArray[Nothing] = new ImmutableArray(new Array[AnyRef](0)).asInstanceOf[ImmutableArray[Nothing]]
}

final class ImmutableArray[@specialized +A: ClassTag] (arr: Array[A]) extends IndexedSeq[A] with IndexedSeqOptimized[A, ImmutableArray[A]] {
  def apply(idx: Int): A = arr(idx)
  def length: Int = arr.length
  override def newBuilder: ImmutableArrayBuilder[A @uncheckedVariance] = new ImmutableArrayBuilder[A](0)
}

final class ImmutableArrayBuilder[@specialized A: ClassTag] (initialSize: Int) extends Builder[A, ImmutableArray[A]] {
  //
  // Note: DO NOT make these private[this] since that doesn't play well with @specialized
  //
  private var arr: Array[A] = if (initialSize > 0) new Array[A](initialSize) else null // Array.empty creates a new array each time so avoid using that
  private var capacity: Int = if (null == arr) 0 else arr.length
  private var length: Int = 0
  @volatile private var done: Boolean = false

  /**
   * The number of items that have been added to this builder
   */
  def size: Int = length

  def +=(elem: A): this.type = {
    assert(!done, "Trying to add to an already closed ImmutableArrayBuilder")
    ensureCapacity(length + 1)
    arr(length) = elem
    length += 1
    this
  }
  
  def result: ImmutableArray[A] = {
    assert(!done, "Calling result again on an already closed ImmutableArrayBuilder")
    
    done = true
    
    if (length == 0) return ImmutableArray.empty
    assert(length <= arr.length, s"Length: $length,  Array.length: ${arr.length}")
    
    val buf: Array[A] = new Array[A](length)
    System.arraycopy(arr, 0, buf, 0, length)
    new ImmutableArray[A](buf)
  }
  
  def clear(): Unit = {
    arr = null
    capacity = 0
    length = 0
    done = false
  }
  
  override def sizeHint(size: Int): Unit = {
    if (capacity < size) resize(size)
  }
  
  private def ensureCapacity(size: Int): Unit = {
    if (capacity < size) {
      var newSize: Int = if (capacity == 0) 16 else capacity * 2
      while (newSize < size) newSize = newSize * 2
      resize(newSize)
    }
  }
  
  private def resize(size: Int): Unit = {
    val buf: Array[A] = new Array[A](size)
    if (length > 0 && null != arr) System.arraycopy(arr, 0, buf, 0, length)
    arr = buf
    capacity = size
  }
}