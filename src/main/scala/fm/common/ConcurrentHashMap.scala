package fm.common

import java.util.concurrent.{ConcurrentHashMap => JavaConcurrentHashMap}
import scala.collection.JavaConverters._
import scala.collection.mutable.Map

/**
 * EXPERIMENTAL - A Scala mutable map that wraps a java ConcurrentHashMap and allows null values
 */
final class ConcurrentHashMap[A,B](map: JavaConcurrentHashMap[A,Option[B]]) extends Map[A,B] {
  def this(initialCapacity: Int, loadFactor: Float, concurrencyLevel: Int) = this(new JavaConcurrentHashMap[A,Option[B]](initialCapacity, loadFactor, concurrencyLevel))
  def this(initialCapacity: Int, loadFactor: Float) = this(initialCapacity, loadFactor, 16)
  def this(initialCapacity: Int) = this(initialCapacity, 0.75f)
  def this() = this(16)
  
  def get(key: A): Option[B] = {
    val v: Option[B] = map.get(key)
    if (v eq null) None else Some(v.getOrElse{ null.asInstanceOf[B] })
  }
  
  def iterator: Iterator[(A,B)] = map.entrySet.iterator.asScala.map{ e => (e.getKey, e.getValue.getOrElse{ null.asInstanceOf[B] }) }
  
  def +=(kv: (A, B)): this.type = {
    update(kv._1, kv._2)
    this
  }
  
  def -=(key: A): this.type = {
    map.remove(key)
    this
  }
  
  override def update(key: A, value: B): Unit = {
    require(null != key, "Key cannot be null")
    map.put(key, Option(value))
  }
}