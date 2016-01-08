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

import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * DEPRECATED - Should use fm.common.Intern instead
 * 
 * An object pool based on a WeakHashMap (using weak key AND weak values) that can be used to
 * return canonical versions of objects.  Once all references to the object go away the WeakHashMap
 * entry will be GC'd.
 *
 * This is similar to how String.intern() works (although probably not as efficient)
 */
@Deprecated
final class WeakObjectPool[T] {
  private[this] val map: WeakHashMap[T,WeakReference[T]] = new WeakHashMap[T,WeakReference[T]]

  /**
   * Returns the canonical version of T
   */
  def apply(value: T): T = synchronized {
    val weakRef: WeakReference[T] = map.get(value)

    if (null != weakRef) {
      val canonical: T = weakRef.get
      if (null != canonical) return canonical
    }

    map.put(value, new WeakReference(value))

    value
  }

  def contains(value: T): Boolean = synchronized{ map.containsKey(value) }

  def clear(): Unit = synchronized{ map.clear() }
}
