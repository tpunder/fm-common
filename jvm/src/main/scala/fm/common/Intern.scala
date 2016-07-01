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

@Deprecated
object Intern {

  @Deprecated
  def apply[T <: AnyRef](value: T): T = value
  
  /**
   * Importing this will enable the intern() method on any Object
   * 
   * NOTE: You should have a proper equals and hashCode implementation
   */
  @Deprecated
  object Implicits {
    @Deprecated
    implicit class RichIntern[T <: AnyRef](private val value: T) extends AnyVal {
      def intern(): T = Intern(value)
    }
  }
}
