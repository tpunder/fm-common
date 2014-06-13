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

import scala.collection.{immutable, mutable, TraversableOnce}

final class RichTraversableOnce[A](val self: TraversableOnce[A]) extends AnyVal {
  
  /**
   * Like groupBy but only allows a single value per key
   */
  def uniqueGroupBy[K](f: A ⇒ K): Map[K, A] = {
    var m = immutable.HashMap.empty[K, A]
    
    for (x <- self) {
      val key: K = f(x)
      require(!m.contains(key), s"Map already contains key: $key   Existing Value: ${m(key)}  Trying to add value: ${x}")
      m = m.updated(key, x)
    }
    
    m
  }
  
  /**
   * A combination of map + find that returns the first Some that is found
   * after applying the map operation.
   */
  @inline def findMapped[B](f: A => Option[B]): Option[B] = {
    self.foreach{ a: A =>
      val b: Option[B] = f(a)
      if(b.isDefined) return b
    }
    
    None
  }
  
  /**
   * Returns a Vector of this Iterable (if it's not already a Vector)
   */
  def toVector: Vector[A] = {
    self match {
      case vector: Vector[_] => vector
      case _ =>
        val b = Vector.newBuilder[A]
        //b.sizeHint(self)
        b ++= self.seq
        b.result
    }
  }
  
  /**
   * Like .toMap but creates an immutable.HashMap
   */
  def toHashMap[T, U](implicit ev: A <:< (T, U)): immutable.HashMap[T, U] = {
    val b = immutable.HashMap.newBuilder[T, U]
    for (x <- self)
      b += x

    b.result
  }
  
  /**
   * Same as .toHashMap but ensures there are no duplicate keys
   */
  def toUniqueHashMap[T, U](implicit ev: A <:< (T, U)): immutable.HashMap[T, U] = {
    var m = immutable.HashMap.empty[T, U]
    
    for (x <- self) {
      val key = x._1
      require(!m.contains(key), s"RichTraversableOnce.toUniqueHashMap - Map already contains key: $key   Existing Value: ${m(key)}  Trying to add value: ${x._2}")
      m += x
    }
    
    m
  }
  
  /**
   * Like .toHashMap except allows multiple values per key
   */
  def toMultiValuedMap[T, U](implicit ev: A <:< (T, U)): immutable.Map[T, Vector[U]] = {
    var m = immutable.HashMap.empty[T, Vector[U]]
    
    for (x <- self) {
      val key: T = x._1
      val value: U = x._2
      
      val values: Vector[U] = m.get(key) match {
        case Some(existing) => existing :+ value
        case None => Vector(value)
      }
      
      m = m.updated(key, values)
    }
    
    m
  }
  
  /**
   * Same as .toMap but ensures there are no duplicate keys
   */
  def toUniqueMap[T, U](implicit ev: A <:< (T, U)): immutable.HashMap[T, U] = toUniqueHashMap(ev)
  
  /**
   * Like .toSet but creates an immutable.HashSet
   */
  def toHashSet: immutable.HashSet[A] = self match {
    case hashSet: immutable.HashSet[_] => hashSet.asInstanceOf[immutable.HashSet[A]]
    case _ =>
      val builder = immutable.HashSet.newBuilder[A]
      builder ++= self
      builder.result
  }
  
  // No idea what this version was for
  //def toHashSet[B >: A]: scala.collection.immutable.HashSet[B] = self.to[scala.collection.immutable.HashSet].asInstanceOf[scala.collection.immutable.HashSet[B]]
  
  /**
   * Like .toHashSet but makes sure there are no duplicates
   */
  def toUniqueHashSet: immutable.HashSet[A] = self match {
    case hashSet: immutable.HashSet[_] => hashSet.asInstanceOf[immutable.HashSet[A]]
    case _ =>
      var set = immutable.HashSet.empty[A]
      
      for (x <- self) {
        require(!set.contains(x), "RichTraversableOnce.toUniqueHashSet - HashSet already contains value: "+x)
        set += x
      }
      
      set  
  }
  
  /**
   * Like .toSet but makes sure there are no duplicates
   */
  def toUniqueSet: immutable.Set[A] = self match {
    case hashSet: immutable.Set[_] => hashSet.asInstanceOf[immutable.Set[A]]
    case _ =>
      var set = immutable.Set.empty[A]
      
      for (x <- self) {
        require(!set.contains(x), "RichTraversableOnce.toUniqueSet - HashSet already contains value: "+x)
        set += x
      }
      
      set
  }
  
  /**
   * Like .toSet but returns a scala.collection.immutable.SortedSet instead
   */
  def toSortedSet(implicit ord: Ordering[A]): immutable.SortedSet[A] = self match {
    case sortedSet: immutable.HashSet[_] => sortedSet.asInstanceOf[immutable.SortedSet[A]]
    case _ =>
      val builder = immutable.SortedSet.newBuilder[A]
      builder ++= self
      builder.result
  }
}