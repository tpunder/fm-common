package fm.common

import java.lang.{Boolean => JavaBoolean}
import java.util.concurrent.{ConcurrentHashMap => JavaConcurrentHashMap}
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * EXPERIMENTAL - A Scala mutable Set based on ConcurrentHashMap
 */
final class ConcurrentHashSet[A](map: JavaConcurrentHashMap[A, JavaBoolean]) extends mutable.Set[A] {
  def this(initialCapacity: Int, loadFactor: Float, concurrencyLevel: Int) = this(new JavaConcurrentHashMap[A, JavaBoolean](initialCapacity, loadFactor, concurrencyLevel))
  def this(initialCapacity: Int, loadFactor: Float) = this(initialCapacity, loadFactor, 16)
  def this(initialCapacity: Int) = this(initialCapacity, 0.75f)
  def this() = this(16)
  
  def asJava: JavaConcurrentHashSet[A] = new JavaConcurrentHashSet(map)
  
  def contains(key: A): Boolean = map.containsKey(key)
  
  def iterator: Iterator[A] = map.keySet().iterator().asScala
  
  def +=(elem: A): this.type = { map.put(elem, JavaBoolean.TRUE); this }
  
  def -=(elem: A): this.type = { map.remove(elem); this }
  
  override def empty: ConcurrentHashSet[A] = new ConcurrentHashSet()
  
  override def foreach[U](f: A => U): Unit = {
    val it = map.keySet.iterator()
    while(it.hasNext) f(it.next)
  }
  
  override def size: Int = map.size()
 
  override def hashCode: Int = map.hashCode
}
