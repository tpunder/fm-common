/*
 * Copyright 2016 Frugal Mechanic (http://frugalmechanic.com)
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

import org.scalajs.dom.raw.{Event, EventTarget}
import org.scalajs.jquery.{JQuery, JQueryEventObject}

trait EventTargetOrTargets extends Any {
  protected def jQueryElements: JQuery
  
  final def onJQueryEvent(tpe: String)(f: JQueryEventObject => Unit): Unit = jQueryElements.on(tpe, f)
  final def oneJQueryEvent(tpe: String)(f: JQueryEventObject => Unit): Unit = jQueryElements.one(tpe, f)
  
  def addEventListener[T <: Event](tpe: String)(f: T => Unit): Unit
  def removeEventListener[T <: Event](tpe: String)(f: T => Unit): Unit
  
  final def addEventListener[T <: Event](tpe: EventType[T])(f: T => Unit): Unit = addEventListener(tpe.name)(f)
  final def removeEventListener[T <: Event](tpe: EventType[T])(f: T => Unit): Unit = removeEventListener(tpe.name)(f)
  
  final def on[T <: Event](tpe: String)(f: T => Unit): Unit = addEventListener(tpe)(f)
  final def on[T <: Event](tpe: EventType[T])(f: T => Unit): Unit = addEventListener(tpe)(f)
  
  /** Only execute the event once */
  final def one[T <: Event](tpe: String)(f: T => Unit): Unit = {
    addEventListener(tpe){ event: T =>
      removeEventListener(tpe)(f)
      f(event)
    }
  }
  
  /** Only execute the event once */
  final def one[T <: Event](tpe: EventType[T])(f: T => Unit): Unit = one(tpe.name)(f)
  
  final def off[T <: Event](tpe: String)(f: T => Unit): Unit = removeEventListener(tpe)(f)
  final def off[T <: Event](tpe: EventType[T])(f: T => Unit): Unit = removeEventListener(tpe)(f)
}