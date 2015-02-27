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

import fm.common.Normalize
import scala.collection.{immutable, mutable, TraversableOnce}

final class RichTraversableOnce[A](val self: TraversableOnce[A]) extends AnyVal {
  /**
   * Like groupBy but returns the count of each key instead of the actual values  
   */
  def countBy[K](f: A => K): Map[K,Int] = {
    var m = immutable.HashMap.empty[K, Int]
    
    for(value <- self) {
      val key: K = f(value)
      m = m.updated(key, m.getOrElse(key, 0) + 1)
    }
    
    m
  }
  
  /**
   * Collapse records that are next to each other by a key
   * 
   * e.g.: This groups evens and odds together:
   * 
   * scala> Vector(2,4,6,1,3,2,5,7).collapseBy{ _ % 2 == 0 }
   * res1: IndexedSeq[(Boolean, Seq[Int])] = Vector((true,Vector(2, 4, 6)), (false,Vector(1, 3)), (true,Vector(2)), (false,Vector(5, 7)))
   */
  def collapseBy[K](f: A => K): IndexedSeq[(K,Seq[A])] = {
    val resBuilder = Vector.newBuilder[(K, Seq[A])]
    
    var isFirst: Boolean = true
    var curKey: K = null.asInstanceOf[K]
    var curBuilder: mutable.Builder[A, Vector[A]] = null
    
    self.foreach { a: A =>
      val key: K = f(a)
      
      if (isFirst) {
        curKey = key
        curBuilder = Vector.newBuilder[A]
        isFirst = false
      }
      
      if (key != curKey) {
        resBuilder += ((curKey, curBuilder.result))
        curKey = key
        curBuilder = Vector.newBuilder
      }
      
      curBuilder += a
    }
    
    if (isFirst) Vector.empty else {
      resBuilder += ((curKey, curBuilder.result))
    }
    
    resBuilder.result
  }
  
  /**
   * Like groupBy but only allows a single value per key
   */
  def uniqueGroupBy[K](f: A ⇒ K): immutable.HashMap[K, A] = {
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
  def toHashMap[K, V](implicit ev: A <:< (K, V)): immutable.HashMap[K, V] = {
    val b = immutable.HashMap.newBuilder[K, V]
    for (x <- self)
      b += x

    b.result
  }
  
  /**
   * Same as .toHashMap but ensures there are no duplicate keys
   */
  def toUniqueHashMap[K, V](implicit ev: A <:< (K, V)): immutable.HashMap[K, V] = {
    var m = immutable.HashMap.empty[K, V]
    
    for (x <- self) {
      val key: K = x._1
      require(!m.contains(key), s"RichTraversableOnce.toUniqueHashMap - Map already contains key: $key   Existing Value: ${m(key)}  Trying to add value: ${x._2}")
      m += x
    }
    
    m
  }
  
  /**
   * Alias of uniqueGroupBy
   */
  def toUniqueHashMapUsing[K](f: A => K): immutable.HashMap[K, A] = uniqueGroupBy(f)
  
  /**
   * Same as toUniqueHashMap but allows you to specify transform functions for the key and value
   */
  def toUniqueHashMapWithTransforms[K, V, K2, V2](keyTransform: K => K2, valueTransform: V => V2)(implicit ev: A <:< (K, V)): immutable.HashMap[K2, V2] = {
    var m = immutable.HashMap.empty[K2, V2]
    
    for (x <- self) {
      val key: K2 = keyTransform(x._1)
      val value: V2 = valueTransform(x._2)
      require(!m.contains(key), s"RichTraversableOnce.toUniqueHashMap - Map already contains key: $key   Existing Value: ${m(key)}  Trying to add value: $value")
      m += ((key, value))
    }
    
    m
  }

  private def identityTransform[T](t: T): T = t
  
  /**
   * Same as toUniqueHashMap but allows you to specify a transform function for the key
   */
  def toUniqueHashMapWithKeyTransform[K, V, K2](keyTransform: K => K2)(implicit ev: A <:< (K, V)): immutable.HashMap[K2, V] = {
    toUniqueHashMapWithTransforms(keyTransform, identityTransform[V])
  }
    
  /**
   * Like .groupBy but gives you an immutable.HashMap[K, IndexedSeq]
   */
  def toMultiValuedMapUsing[K](toKey: A => K): immutable.HashMap[K, IndexedSeq[A]] = {
    var m = immutable.HashMap.empty[K, Vector[A]]
    
    for (value <- self) {
      val key: K = toKey(value)
      
      val values: Vector[A] = m.get(key) match {
        case Some(existing) => existing :+ value
        case None => Vector(value)
      }
      
      m = m.updated(key, values)
    }
    
    m
  }
  
  /**
   * Like .toHashMap except allows multiple values per key
   * 
   * TODO: Change this to IndexedSeq so we can switch to ImmutableArray (if we want to)
   */
  def toMultiValuedMap[K, V](implicit ev: A <:< (K, V)): immutable.HashMap[K, Vector[V]] = {
    var m = immutable.HashMap.empty[K, Vector[V]]
    
    for (x <- self) {
      val key: K = x._1
      val value: V = x._2
      
      val values: Vector[V] = m.get(key) match {
        case Some(existing) => existing :+ value
        case None => Vector(value)
      }
      
      m = m.updated(key, values)
    }
    
    m
  }
  
  /**
   * Same as toMultiValuedMap but allows you to specify transform functions for the key and value
   * 
   * TODO: Change this to IndexedSeq so we can switch to ImmutableArray (if we want to)
   */
  def toMultiValuedMapWithTransforms[K, V, K2, V2](keyTransform: K => K2, valueTransform: V => V2)(implicit ev: A <:< (K, V)): immutable.HashMap[K2, Vector[V2]] = {
    var m = immutable.HashMap.empty[K2, Vector[V2]]
    
    for (x <- self) {
      val key: K2 = keyTransform(x._1)
      val value: V2 = valueTransform(x._2)
      
      val values: Vector[V2] = m.get(key) match {
        case Some(existing) => existing :+ value
        case None => Vector(value)
      }
      
      m = m.updated(key, values)
    }
    
    m
  }
  
  /**
   * Same as toMultiValuedMap but allows you to specify a transform function for the key
   * 
   * TODO: Change this to IndexedSeq so we can switch to ImmutableArray (if we want to)
   */
  def toMultiValuedMapWithKeyTransform[K, V, K2](keyTransform: K => K2)(implicit ev: A <:< (K, V)): immutable.HashMap[K2, Vector[V]] = {
    toMultiValuedMapWithTransforms(keyTransform, identityTransform[V])
  }
  
  /**
   * Same as .toMap but ensures there are no duplicate keys
   */
  def toUniqueMap[K, V](implicit ev: A <:< (K, V)): immutable.HashMap[K, V] = toUniqueHashMap(ev)
  
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

  def toUniqueLowerAlphaNumericMap[V](implicit ev: A <:< (String,V)): immutable.HashMap[String, V] = toUniqueHashMapWithKeyTransform(Normalize.lowerAlphanumeric)
  def toUniqueURLNameMap[V](implicit ev: A <:< (String,V)): immutable.HashMap[String, V] = toUniqueHashMapWithKeyTransform(Normalize.urlName)
  
  // TODO: Change this to IndexedSeq so we can switch to ImmutableArray (if we want to)
  def toMultiValuedLowerAlphaNumericMap[V](implicit ev: A <:< (String,V)): immutable.HashMap[String, Vector[V]] = toMultiValuedMapWithKeyTransform(Normalize.lowerAlphanumeric)
  
  // TODO: Change this to IndexedSeq so we can switch to ImmutableArray (if we want to)
  def toMultiValuedURLNameMap[V](implicit ev: A <:< (String,V)): immutable.HashMap[String, Vector[V]] = toMultiValuedMapWithKeyTransform(Normalize.urlName)
}