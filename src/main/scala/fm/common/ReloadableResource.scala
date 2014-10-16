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

import java.io.{File, InputStream}
import java.util.{Timer, TimerTask}

object ReloadableResource {
  private val timer: Timer = new Timer("ReloadableResource Check", true /* isDaemon */)
}

abstract class ReloadableResource[T] extends Logging {
  /** Load the resource from it's primary source */
  protected def loadFromPrimary(): Option[T]
  
  /** Load the resource from it's backup source (if any) */
  protected def loadFromBackup(): Option[T]
  
  /** A backup backup resource that will be used if the files and backup cannot be loaded */
  protected def defaultResource: Option[T]
  
  /** The Last Modified time of the resource (can be set to System.currentTimeMillis to always reload) */
  protected def lookupLastModified(): Long

  @volatile private[this] var _current: T = null.asInstanceOf[T]
  
  private lazy val init: Unit = {
    _current = loadResource()
  }
  
  /** Get the current version of the resource */
  final def apply(): T = {
    init
    _current
  }
  
  /** Attempt to reload the current resource.  If there is a problem the existing version will be left in place */
  final def reload(): Unit = tryLoadResource(tryBackup = false).foreach { resource: T => _current = resource }
  
  /** Directly load the resource and return the result.  Doesn't touch the current resource in this class. */
  final def loadResource(): T = (tryLoadResource(tryBackup = true) orElse defaultResource).getOrElse{ throw new Exception("Unable to load resource") }

  /**
   * Enable checking and automatic reload of the resource if the external file is updated
   */
  final def enableAutoUpdateCheck(delaySeconds: Int = 300, periodSeconds: Int = 300): Unit = {
    ReloadableResource.timer.schedule(ResourceCheckTimerTask, delaySeconds.toLong * 1000L, periodSeconds.toLong * 1000L)
  }
  
  /** Disable the auto update checks */
  final def disableAutoUpdateCheck(): Unit = ResourceCheckTimerTask.cancel()
  
  private def tryLoadResource(tryBackup: Boolean): Option[T] = {
    
    try {
      val (millis, result): (Long, Option[T]) = Util.time(loadFromPrimary())
      if (result.isDefined) {
        logger.info(s"Loaded resource from primary source ($millis ms)")
        return result
      }
    } catch {
      case ex: Exception => logger.error("Exception Loading Resource from primary source", ex)
    }
    
    if (!tryBackup) return None
    
    try {
      val (millis, result): (Long, Option[T]) = Util.time(loadFromBackup())
      if (result.isDefined) {
        logger.info(s"Loaded resource from backup source ($millis ms)")
        return result
      }
    } catch {
      case ex: Exception => logger.error("Exception Loading Resource from backup source", ex)
    }

    None
  }
  
  private object ResourceCheckTimerTask extends TimerTask {
    /**
     * This should be the lastModified date of the currently loaded resource
     */
    private[this] var lastModified: Long = lookupLastModified()

    def run() {
      val currentLastModified = lookupLastModified()
      if (currentLastModified != lastModified) {
        logger.info("Detected Updated Resource, Reloading...")
        reload()
        lastModified = currentLastModified
      }
    }
  }
}