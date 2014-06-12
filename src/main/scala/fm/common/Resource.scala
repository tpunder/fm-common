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

import java.io.IOException

object Resource {
  private[this] val logger = Logger.getLogger(this) 
  
  def using[T, R <% AutoCloseable](resource: R)(f: R => T): T = try {
    f(resource)
  } catch {
    case ex: Exception =>
      logger.debug("Caught exception using resource", ex)
      throw ex
  } finally {
    if(null != resource) resource.close()
  }
  
  def using[T, R <% AutoCloseable](resources: Seq[R])(f: Seq[R] => T): T = try {
    f(resources)
  } catch {
    case ex: Exception =>
      logger.debug("Caught exception using resources", ex)
      throw ex
  } finally {
    if(null != resources) resources.foreach{r => if(null != r) r.close() }
  }
  
  implicit def toCloseable[T <: { def close(): Unit }](obj: T): AutoCloseable = new AutoCloseable {
    import scala.language.reflectiveCalls
    
    // This causes a reflective call
    def close(): Unit = obj.close()
  }
  
  def apply[T <: AutoCloseable](resource: T): Resource[T] = toResource(resource)
  
  implicit def toResource[T <: AutoCloseable](resource: T): Resource[T] = SingleUseResource(resource)
  
  val empty: Resource[Unit] = UnitResource
  
  //
  // Helpers for using multiple resource
  //
  
  def use[T,A](a: Resource[A])(fun: A => T): T = a.use{ aa => fun(aa) }
  def use[T,A,B](a: Resource[A], b: Resource[B])(fun: (A,B) => T): T = a.use{ aa => b.use{ bb => fun(aa,bb) } }
  def use[T,A,B,C](a: Resource[A], b: Resource[B], c: Resource[C])(fun: (A,B,C) => T): T = a.use{ aa => b.use{ bb => c.use{ cc => fun(aa,bb,cc) } } }
  def use[T,A,B,C,D](a: Resource[A], b: Resource[B], c: Resource[C], d: Resource[D])(fun: (A,B,C,D) => T): T = a.use{ aa => b.use{ bb => c.use{ cc => d.use { dd => fun(aa,bb,cc,dd) } } } }
  def use[T,A,B,C,D,E](a: Resource[A], b: Resource[B], c: Resource[C], d: Resource[D], e: Resource[E])(fun: (A,B,C,D,E) => T): T = a.use{ aa => b.use{ bb => c.use{ cc => d.use { dd => e.use { ee => fun(aa,bb,cc,dd,ee) } } } } }
  def use[T,A,B,C,D,E,F](a: Resource[A], b: Resource[B], c: Resource[C], d: Resource[D], e: Resource[E], f: Resource[F])(fun: (A,B,C,D,E,F) => T): T = a.use{ aa => b.use{ bb => c.use{ cc => d.use { dd => e.use { ee => f.use{ ff => fun(aa,bb,cc,dd,ee,ff) } } } } } }
}

/**
 * An Automatically Managed Resource that can either be used once (e.g. reading an input stream) or multiple times (e.g. reading a file).
 * 
 * The purpose of Resource is two-fold:
 * 1 - To automatically handle closing a resource after it is done being used.
 * 2 - To abstract the fact that some resources can be read multiple times while other resources are one-time use.
 */
trait Resource[+A] {
  def use[T](f: A => T): T
  
  /** Is this resource usable?  i.e. will the use() method work? */
  def isUsable: Boolean
  
  /** Can this resource be used multiple times? */
  def isMultiUse: Boolean
  
  final def map[B](f: A => B): Resource[B] = new MappedResource(this, f)
  
  final def flatMap[B](f: A => Resource[B]): Resource[B] = new FlatMappedResource(this, f)
  
  final def foreach[U](f: A => U): Unit = use(f)
}

/**
 * An empty resource
 */
object UnitResource extends Resource[Unit] {
  def use[T](f: Unit => T): T = f(Unit)
  
  def isUsable: Boolean = true
  def isMultiUse: Boolean = true
}

/**
 * A Dummy Resource that does nothing
 */
final case class DummyResource[A](a: A) extends Resource[A] {
  def use[T](f: A => T): T = f(a)
  def isUsable: Boolean = true
  def isMultiUse: Boolean = false
}


object MultiUseResource {
  def apply[A <% AutoCloseable](makeResource: => A) = new MultiUseResource(makeResource)
}

/**
 * A Resource that can be used multiple times (e.g. opening an InputStream or Reader for a File)
 */
final class MultiUseResource[+A <% AutoCloseable](makeResource: => A) extends Resource[A] {
  final def isUsable: Boolean = true
  final def isMultiUse: Boolean = true
  
  final def use[T](f: A => T): T = Resource.using(makeResource)(f)
}

object SingleUseResource {
  def apply[A <% AutoCloseable](resource: A) = new SingleUseResource(resource)
}

/**
 * A Resource that can only be used once (e.g. reading an InputStream)
 */
final class SingleUseResource[+A <% AutoCloseable](resource: A) extends Resource[A] {
  @volatile private[this] var used: Boolean = false
  
  final def isUsable: Boolean = !used
  final def isMultiUse: Boolean = false
  
  final def use[T](f: A => T): T = {
    if(used) throw new IOException("The SingleUseResource has already been used and cannot be used again")
    used = true
    Resource.using(resource)(f)
  }
}

/**
 * For Resource.map
 */
final class MappedResource[A, B](resource: Resource[A], mapping: A => B) extends Resource[B] {
  def use[T](f: B => T): T = resource.use{ a => f(mapping(a)) }
  
  def isUsable = resource.isUsable
  def isMultiUse: Boolean = resource.isMultiUse
}

/**
 * For Resource.flatMap
 */
final class FlatMappedResource[A, B](resource: Resource[A], mapping: A => Resource[B]) extends Resource[B] {
  def use[T](f: B => T): T = resource.use{ a => mapping(a).use[T](f) }
  
  def isUsable = resource.isUsable
  def isMultiUse: Boolean = resource.isMultiUse
}