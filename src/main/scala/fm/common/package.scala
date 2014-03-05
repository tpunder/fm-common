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
package fm

import scala.util.Try

package object common extends Implicits {
  type URL = java.net.URL
  type URI = java.net.URI
  
  object URL {
    def apply(url: String): URL = new java.net.URL(url)
    def tryParse(url: String): Try[URL] = Try{ apply(url) }
    def get(url: String): Option[URL] = tryParse(url).toOption
  }
  
  object URI {
    def apply(uri: String): URI = new java.net.URI(uri)
    def tryParse(uri: String): Try[URI] = Try{ apply(uri) }
    def get(uri: String): Option[URI] = tryParse(uri).toOption
  }
}