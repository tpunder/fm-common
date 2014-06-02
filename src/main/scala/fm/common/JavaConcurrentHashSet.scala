package fm.common

import java.lang.{Boolean => JavaBoolean}
import java.util.{Collection, Iterator => JavaIterator, Set}
import java.util.concurrent.{ConcurrentHashMap => JavaConcurrentHashMap}
import scala.collection.JavaConverters._

/**
 * EXPERIMENTAL - A Java Set based on ConcurrentHashMap
 */
final class JavaConcurrentHashSet[A](map: JavaConcurrentHashMap[A, JavaBoolean]) extends Set[A] {
  def this(initialCapacity: Int, loadFactor: Float, concurrencyLevel: Int) = this(new JavaConcurrentHashMap[A, JavaBoolean](initialCapacity, loadFactor, concurrencyLevel))
  def this(initialCapacity: Int, loadFactor: Float) = this(initialCapacity, loadFactor, 16)
  def this(initialCapacity: Int) = this(initialCapacity, 0.75f)
  def this() = this(16)
  
  def asScala: ConcurrentHashSet[A] = new ConcurrentHashSet(map)
  
  //
  // java.util.Set implementation
  //
  def add(elem: A): Boolean = null == map.put(elem, JavaBoolean.TRUE)
  
  def addAll(col: Collection[_ <: A]): Boolean = {
    var changed: Boolean = false
    val it: JavaIterator[_ <: A] = col.iterator()
    while(it.hasNext) {
      if (add(it.next)) changed = true
    }
    changed
  }
  
  def clear(): Unit = map.clear()
  
  def contains(obj: Object): Boolean = map.containsKey(obj)
  
  def containsAll(col: Collection[_]): Boolean = {
    val it: JavaIterator[_] = col.iterator()
    while(it.hasNext) {
      if (!contains(it.next.asInstanceOf[Object])) return false
    }
    true
  }
  
  //override def equals(other: Object): Boolean = ???
  
  override def hashCode(): Int = map.hashCode
  
  def isEmpty: Boolean = map.isEmpty()
  
  def iterator: JavaIterator[A] = map.keySet.iterator
  
  def remove(obj: Object): Boolean = map.remove(obj, JavaBoolean.TRUE)
  
  def removeAll(col: Collection[_]): Boolean = {
    var changed: Boolean = false
    val it: JavaIterator[_] = col.iterator()
    while(it.hasNext) {
      if (remove(it.next.asInstanceOf[Object])) changed = true
    }
    changed
  }
  
  def retainAll(col: Collection[_]): Boolean = throw new UnsupportedOperationException()
  
  def size: Int = map.size()
  
  def toArray: Array[Object] = map.keySet.toArray
  def toArray[T](a: Array[T with Object]): Array[T with Object] = map.keySet.toArray[T](a)
}