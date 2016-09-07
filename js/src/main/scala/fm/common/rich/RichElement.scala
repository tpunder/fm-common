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
package fm.common.rich

import fm.common.Implicits._
import fm.common.UserDataAttributes
import org.scalajs.dom.window
import org.scalajs.dom.raw.{CSSStyleDeclaration, Element}

final class RichElement(val elem: Element) extends AnyVal {
  
  /**
   * Helpers for accessing the "user-" attributes
   * 
   * https://developer.mozilla.org/en-US/docs/Web/Guide/HTML/Using_data_attributes
   */
  def data: UserDataAttributes = new UserDataAttributes(elem)

  /** Shortcut for window.getComputedStyle(elem) */
  def computedStyle: CSSStyleDeclaration = window.getComputedStyle(elem)
  
  /** Shortcut for window.getComputedStyle(elem, pseudoElem) */
  def computedStyle(pseudoElem: String): CSSStyleDeclaration = window.getComputedStyle(elem, pseudoElem)
  
  /** Shortcut for window.getComputedStyle(elem, pseudoElem) */
  def computedStyle(pseudoElem: Option[String]): CSSStyleDeclaration = pseudoElem match {
    case Some(pseudo) => computedStyle(pseudo)
    case None => computedStyle
  }
  
  def hasClass(className: String): Boolean = elem.classList.contains(className)
  
  def addClass(className: String): Unit = if (!elem.classList.contains(className)) elem.classList.add(className)
  
  def removeClass(className: String): Unit = elem.classList.remove(className)
}
