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

import com.google.common.cache.{Cache => GoogleCache, CacheBuilder => GoogleCacheBuilder, CacheLoader => GoogleCacheLoader, CacheStats => GoogleCacheStats, LoadingCache => GoogleLoadingCache, RemovalCause => GoogleRemovalCause, RemovalListener => GoogleRemovalListener, RemovalNotification => GoogleRemovalNotification}
import com.google.common.util.concurrent.ListenableFuture
import java.lang.{Iterable => JavaIterable}
import java.util.{Map => JavaMap}
import java.util.concurrent.{Callable, ConcurrentMap, Executor, TimeUnit}
import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

/**
 * Wrapper for Google Guava's Cache classes
 */
object Cache {
  /** Mirrors com.google.common.cache.RemovalNotification */
  final case class RemovalNotification[K,V](key: K, value: V, cause: RemovalCause)
  
  /** Mirrors com.google.common.cache.RemovalCause */
  sealed abstract class RemovalCause(val wasEvicted: Boolean)
  
  case object COLLECTED extends RemovalCause(true)
  case object EXPIRED extends RemovalCause(true)
  case object EXPLICIT extends RemovalCause(false)
  case object REPLACED extends RemovalCause(false)
  case object SIZE extends RemovalCause(true)
  
  final case class CacheStats(private val stats: GoogleCacheStats) {
    /** Returns the average time spent loading new values. */
    def averageLoadPenalty: Double = stats.averageLoadPenalty()
    
    /** Returns the number of times an entry has been evicted. */
    def evictionCount: Long = stats.evictionCount()
    
    /** Returns the number of times Cache lookup methods have returned a cached value. */
    def hitCount: Long = stats.hitCount()
    
    /** Returns the ratio of cache requests which were hits. */
    def hitRate: Double = stats.hitRate()
    
    /** Returns the total number of times that Cache lookup methods attempted to load new values. */
    def loadCount: Long = stats.loadCount()
    
    /** Returns the number of times Cache lookup methods threw an exception while loading a new value. */
    def loadExceptionCount: Long = stats.loadExceptionCount()
    
    /** Returns the ratio of cache loading attempts which threw exceptions. */
    def loadExceptionRate: Double = stats.loadExceptionRate()
    
    /** Returns the number of times Cache lookup methods have successfully loaded a new value. */
    def loadSuccessCount: Long = stats.loadSuccessCount()
    
    /** Returns a new CacheStats representing the difference between this CacheStats and other. */
    def minus(other: CacheStats): CacheStats = CacheStats(stats.minus(other.stats))
    
    /** Returns the number of times Cache lookup methods have returned an uncached (newly loaded) value, or null. */
    def missCount: Long = stats.missCount()
    
    /** Returns the ratio of cache requests which were misses. */
    def missRate: Double = stats.missRate()
    
    /** Returns a new CacheStats representing the sum of this CacheStats and other. */
    def plus(other: CacheStats): CacheStats = CacheStats(stats.plus(other.stats))
    
    /** Returns the number of times Cache lookup methods have returned either a cached or uncached value. */
    def requestCount: Long = stats.requestCount()
    
    /** Returns the total number of nanoseconds the cache has spent loading new values. */
    def totalLoadTime: Long = stats.totalLoadTime()
    
    override def toString(): String = stats.toString()
  }
  
  private class GoogleRemovalListenerAdapter[K,V](removalListener: RemovalNotification[K,V] => Unit) extends GoogleRemovalListener[K,V] {
    def onRemoval(notification: GoogleRemovalNotification[K,V]): Unit = {
      val cause: Cache.RemovalCause = notification.getCause() match {
        case GoogleRemovalCause.COLLECTED => Cache.COLLECTED
        case GoogleRemovalCause.EXPIRED => Cache.EXPIRED
        case GoogleRemovalCause.EXPLICIT => Cache.EXPLICIT
        case GoogleRemovalCause.REPLACED => Cache.REPLACED
        case GoogleRemovalCause.SIZE => Cache.SIZE
      }
      
      removalListener(Cache.RemovalNotification(notification.getKey(), notification.getValue(), cause))
    }
  }
  
  /**
   * A simplified wrapper around com.google.common.cache.CacheBuilder
   * 
   * @param initialCapacity Sets the minimum total size for the internal hash tables.
   * @param maxSize Specifies the maximum number of entries the cache may contain.
   * @param concurrencyLevel Guides the allowed concurrency among update operations.
   * @param expireAfterAccess Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after the entry's creation, the most recent replacement of its value, or its last access.
   * @param expireAfterWrite Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after the entry's creation, or the most recent replacement of its value.
   * @param recordStats Enable the accumulation of CacheStats during the operation of the cache.
   * @param refreshAfterWrite Specifies that active entries are eligible for automatic refresh once a fixed duration has elapsed after the entry's creation, or the most recent replacement of its value.
   * @param weakKeys Specifies that each key (not value) stored in the cache should be wrapped in a WeakReference (by default, strong references are used).
   * @param weakValues Specifies that each value (not key) stored in the cache should be wrapped in a WeakReference (by default, strong references are used).
   * @param softValues Specifies that each value (not key) stored in the cache should be wrapped in a SoftReference (by default, strong references are used).
   * @param removalListener Specifies a listener instance that caches should notify each time an entry is removed for any reason.
   */
  def apply[K,V](
    initialCapacity: Int = -1,
    maxSize: Long = -1L,
    concurrencyLevel: Int = -1,
    expireAfterAccess: Duration = Duration.Inf,
    expireAfterWrite: Duration = Duration.Inf,
    refreshAfterWrite: Duration = Duration.Inf,
    recordStats: Boolean = false,
    weakKeys: Boolean = false,
    weakValues: Boolean = false,
    softValues: Boolean = false,
    removalListener: Cache.RemovalNotification[K,V] => Unit = null
  ): Cache[K,V] = {
    val b: GoogleCacheBuilder[Object,Object] = makeBuilder(
      initialCapacity = initialCapacity,
      maxSize = maxSize,
      concurrencyLevel = concurrencyLevel,
      expireAfterAccess = expireAfterAccess,
      expireAfterWrite = expireAfterWrite,
      refreshAfterWrite = refreshAfterWrite,
      recordStats = recordStats,
      weakKeys = weakKeys,
      weakValues = weakValues,
      softValues = softValues,
      removalListener = removalListener
    )
    
    new Cache(b.build().asInstanceOf[GoogleCache[K,V]])
  }
  
  private[common] def makeBuilder[K,V](
    initialCapacity: Int,
    maxSize: Long,
    concurrencyLevel: Int,
    expireAfterAccess: Duration,
    expireAfterWrite: Duration,
    refreshAfterWrite: Duration,
    recordStats: Boolean,
    weakKeys: Boolean,
    weakValues: Boolean,
    softValues: Boolean,
    removalListener: Cache.RemovalNotification[K,V] => Unit
  ): GoogleCacheBuilder[Object,Object] = {
    val b: GoogleCacheBuilder[Object,Object] = GoogleCacheBuilder.newBuilder()
    
    if (initialCapacity >= 0) b.initialCapacity(initialCapacity)
    if (maxSize >= 0L) b.maximumSize(maxSize)
    if (concurrencyLevel >= 0) b.concurrencyLevel(concurrencyLevel)
    if (expireAfterAccess.isFinite) b.expireAfterAccess(expireAfterAccess.length, expireAfterAccess.unit)
    if (expireAfterWrite.isFinite) b.expireAfterWrite(expireAfterWrite.length, expireAfterWrite.unit)
    if (refreshAfterWrite.isFinite) b.refreshAfterWrite(refreshAfterWrite.length, refreshAfterWrite.unit)
    if (recordStats) b.recordStats()
    if (weakKeys) b.weakKeys()
    if (weakValues) b.weakValues()
    if (softValues) b.softValues()
    if (null ne removalListener) b.removalListener(new GoogleRemovalListenerAdapter[K,V](removalListener))
    
    b
  }
}

/**
 * Wrapper around com.google.common.cache.Cache
 */
sealed class Cache[K,V](cache: GoogleCache[K,V]) {
  
  /** Returns a view of the entries stored in this cache as a thread-safe map. */
  def asMap(): ConcurrentMap[K,V] = cache.asMap()
  
  /** Performs any pending maintenance operations needed by the cache. */
  def cleanUp(): Unit = cache.cleanUp()
  
  /** Returns the value associated with key in this cache, obtaining that value from valueLoader if necessary. */
  def get(key: K, valueLoader: => V): V = cache.get(key, new Callable[V]{ def call: V = valueLoader })
  
  /** Returns a map of the values associated with keys in this cache. */
  def getAllPresent(keys: Iterable[K]): Map[K,V] = cache.getAllPresent(keys.asJava).asScala.toMap
  
  /** Returns the value associated with key in this cache, or None if there is no cached value for key. */
  def getIfPresent(key: K): Option[V] = Option(cache.getIfPresent(key))
  
  /** Discards any cached value for key key. */
  def invalidate(key: K): Unit = cache.invalidate(key)
  
  /** Discards all entries in the cache. */
  def invalidateAll(): Unit = cache.invalidateAll()
  
  /** Discards any cached values for keys keys. */
  def invalidateAll(keys: Iterable[K]): Unit = cache.invalidateAll(keys.asJava)
  
  /** Associates value with key in this cache. */
  def put(key: K, value: V): Unit = cache.put(key, value)
  
  /** Copies all of the mappings from the specified map to the cache. */
  def putAll(entries: Traversable[(K,V)]): Unit = entries.foreach{ case (k,v) => put(k, v) }
  
  /** Returns the approximate number of entries in this cache. */
  def size(): Long = cache.size()
  
  /** Returns a current snapshot of this cache's cumulative statistics. */
  def stats(): Cache.CacheStats = Cache.CacheStats(cache.stats())
}

object LoadingCache {
  /**
   * A simplified wrapper around com.google.common.cache.CacheBuilder
   * 
   * @param initialCapacity Sets the minimum total size for the internal hash tables.
   * @param maxSize Specifies the maximum number of entries the cache may contain.
   * @param concurrencyLevel Guides the allowed concurrency among update operations.
   * @param expireAfterAccess Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after the entry's creation, the most recent replacement of its value, or its last access.
   * @param expireAfterWrite Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after the entry's creation, or the most recent replacement of its value.
   * @param recordStats Enable the accumulation of CacheStats during the operation of the cache.
   * @param refreshAfterWrite Specifies that active entries are eligible for automatic refresh once a fixed duration has elapsed after the entry's creation, or the most recent replacement of its value.
   * @param weakKeys Specifies that each key (not value) stored in the cache should be wrapped in a WeakReference (by default, strong references are used).
   * @param weakValues Specifies that each value (not key) stored in the cache should be wrapped in a WeakReference (by default, strong references are used).
   * @param softValues Specifies that each value (not key) stored in the cache should be wrapped in a SoftReference (by default, strong references are used).
   * @param removalListener Specifies a listener instance that caches should notify each time an entry is removed for any reason.
   */
  def apply[K <: AnyRef,V <: AnyRef](
    initialCapacity: Int = -1,
    maxSize: Long = -1L,
    concurrencyLevel: Int = -1,
    expireAfterAccess: Duration = Duration.Inf,
    expireAfterWrite: Duration = Duration.Inf,
    refreshAfterWrite: Duration = Duration.Inf,
    recordStats: Boolean = false,
    weakKeys: Boolean = false,
    weakValues: Boolean = false,
    softValues: Boolean = false,
    removalListener: Cache.RemovalNotification[K,V] => Unit = null
  ): CacheLoaderBuilder = {
    val b: GoogleCacheBuilder[Object,Object] = Cache.makeBuilder(
      initialCapacity = initialCapacity,
      maxSize = maxSize,
      concurrencyLevel = concurrencyLevel,
      expireAfterAccess = expireAfterAccess,
      expireAfterWrite = expireAfterWrite,
      refreshAfterWrite = refreshAfterWrite,
      recordStats = recordStats,
      weakKeys = weakKeys,
      weakValues = weakValues,
      softValues = softValues,
      removalListener = removalListener
    )
    
    new CacheLoaderBuilder(b)
  }
  
  def apply[K <: AnyRef,V <: AnyRef](loader: K => V): LoadingCache[K,V] = apply()(loader)
  
  def apply[K <: AnyRef,V <: AnyRef](loader: (K, Option[V]) => V): LoadingCache[K,V] = apply()(loader)
  
  def apply[K <: AnyRef,V <: AnyRef](loader: CacheLoader[K,V]): LoadingCache[K,V] = apply()(loader)
  
  final class CacheLoaderBuilder(builder: GoogleCacheBuilder[Object,Object]) {
    def apply[K <: AnyRef,V <: AnyRef](loader: K => V): LoadingCache[K,V] = apply(CacheLoader(loader))
    
    def apply[K <: AnyRef,V <: AnyRef](loader: (K, Option[V]) => V): LoadingCache[K,V] = apply(CacheLoader(loader))
    
    def apply[K <: AnyRef,V <: AnyRef](loader: CacheLoader[K,V]): LoadingCache[K,V] = {
      new LoadingCache(builder.build(new GoogleCacheLoaderAdapter(loader)).asInstanceOf[GoogleLoadingCache[K,V]])
    }
  }
  
  final class InvalidCacheLoadException(msg: String, cause: Throwable, stackTrace: Array[StackTraceElement]) extends RuntimeException(msg, cause) {
    override def fillInStackTrace: Throwable = {
      setStackTrace(stackTrace)
      this
    }
  }
  
  object CacheLoader {
    def apply[K,V](loader: K => V): CacheLoader[K,V] = new CacheLoader[K,V] {
      def load(key: K): V = loader(key)
    }
    
    /**
     * Includes synchronous reload with old value
     */
    def apply[K,V](loader: (K, Option[V]) => V): CacheLoader[K,V] = new CacheLoader[K,V] {
      def load(key: K): V = loader(key, None)
      override def reload(key: K, oldValue: V): Future[V] = Future.successful(loader(key, Option(oldValue)))
    } 
  }
  
  /** Mirrors com.google.common.cache.CacheLoader */
  abstract class CacheLoader[K,V] {
    def load(key: K): V
    def loadAll(keys: Iterable[K]): Map[K,V] = ??? // This is intentional to trigger the default GoogleCacheLoader behavior if this method is not overridden
    def reload(key: K, oldValue: V): Future[V] = Future.successful(load(key))
  }
  
  private class GoogleCacheLoaderAdapter[K,V](loader: CacheLoader[K,V]) extends GoogleCacheLoader[K,V] {
    def load(key: K): V = loader.load(key)
    override def loadAll(keys: JavaIterable[_ <: K]): JavaMap[K,V] = try { loader.loadAll(keys.asScala).asJava } catch { case _: NotImplementedError => super.loadAll(keys) }
    override def reload(key: K, oldValue: V): ListenableFuture[V] = new GoogleListenableFutureAdapter[V](loader.reload(key, oldValue))
  }
  
  private class GoogleListenableFutureAdapter[V](future: Future[V]) extends ListenableFuture[V] {
    def addListener(listener: Runnable, executor: Executor): Unit = future.onComplete{ _ => listener.run() }(ExecutionContext.fromExecutor(executor))
    def cancel(mayInterruptIfRunning: Boolean): Boolean = false
    def get(): V = Await.result(future, Duration.Inf)
    def get(timeout: Long, unit: TimeUnit): V = Await.result(future, Duration(timeout, unit))
    def isCancelled(): Boolean = false
    def isDone(): Boolean = future.isCompleted
  }
  
  private val exceptionHandler: PartialFunction[Throwable,Nothing] = {
    case ex: GoogleCacheLoader.InvalidCacheLoadException => throw new InvalidCacheLoadException(ex.getMessage, ex.getCause, ex.getStackTrace)
    case other => throw other
  }
}

/**
 * Wrapper around com.google.common.cache.LoadingCache
 */
final class LoadingCache[K,V](cache: GoogleLoadingCache[K,V]) extends Cache(cache) {
  
  /** Returns the value associated with key in this cache, first loading that value if necessary. */
  def get(key: K): V = try { cache.get(key) } catch LoadingCache.exceptionHandler
  
  /** Returns a map of the values associated with keys, creating or retrieving those values if necessary. */
  def getAll(keys: Iterable[K]): Map[K,V] = cache.getAll(keys.asJava).asScala.toMap
  
  /** Loads a new value for key key, possibly asynchronously. */
  def refresh(key: K): Unit = cache.refresh(key)
}