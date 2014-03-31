package fm.common

import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * An object pool based on a WeakHashMap (using weak key AND weak values) that can be used to
 * return canonical versions of objects.  Once all references to the object go away the WeakHashMap
 * entry will be GC'd.
 *
 * This is similar to how String.intern() works (although probably not as efficient)
 */
final class WeakObjectPool[T] {
  private val map: WeakHashMap[T,WeakReference[T]] = new WeakHashMap[T,WeakReference[T]]

  /**
   * Returns the canonical version of T
   */
  def apply(value: T): T = synchronized {
    val weakRef: WeakReference[T] = map.get(value)

    if(null != weakRef) {
      val canonical: T = weakRef.get
      if(null != canonical) return canonical
    }

    map.put(value, new WeakReference(value))

    value
  }

  def contains(value: T): Boolean = synchronized{ map.containsKey(value) }

  def clear(): Unit = synchronized{ map.clear }
}
