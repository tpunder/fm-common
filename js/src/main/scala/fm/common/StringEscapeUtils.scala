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

import scala.scalajs.js.URIUtils

object StringEscapeUtils extends StringEscapeUtilsBase {
  
  // JavaScript's encodeURIComponent doesn't convert the same chars as the JVMs URLEncoder so 
  // we convert those ourselves.  See notes on https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent
  def encodeURIComponent(s: String): String = URIUtils.encodeURIComponent(s).replace("%20","+").flatMap{
    case ch @ ('!' | '\'' | '(' | ')' | '*') => "%"+ch.toInt.toHexString
    case ch => ch.toString
  }
  
  // Convert + back into %20
  def decodeURIComponent(s: String): String = URIUtils.decodeURIComponent(s.replace("+","%20"))
}