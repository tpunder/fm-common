/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 by Lloyd Chan
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * This is from: https://github.com/lloydmeta/enumeratum
 */
package fm.common

import scala.language.experimental.macros
import scala.language.postfixOps

/**
 * All the cool kids have their own Enumeration implementation, most of which try to
 * do so in the name of implementing exhaustive pattern matching.
 *
 * This is yet another one.
 *
 * How to use:
 *
 * {{{
 * sealed trait DummyEnum
 *
 * object DummyEnum extends Enum[DummyEnum] {
 *
 * val values = findValues
 *
 * case object Hello extends DummyEnum
 * case object GoodBye extends DummyEnum
 * case object Hi extends DummyEnum
 *
 * }
 *
 *
 * DummyEnum.values should be(Set(Hello, GoodBye, Hi))
 *
 * DummyEnum.withName("Hello") should be(Hello)
 * }}}
 * @tparam A The sealed trait
 */
trait Enum[A <: EnumEntry] {

  /**
    * Map of [[A]] object names to [[A]]s
   */
  lazy final val namesToValuesMap: Map[String, A] = values map (v => v.entryName -> v) toMap
  /**
    * Map of [[A]] object names in lower case to [[A]]s for case-insensitive comparison
   */
  lazy final val lowerCaseNamesToValuesMap: Map[String, A] = values map (v => v.entryName.toLowerCase -> v) toMap
  /**
    * Map of [[A]] to their index in the values sequence.
    *
    * A performance optimisation so that indexOf can be found in constant time.
    */
  lazy final val valuesToIndex: Map[A, Int] = values.zipWithIndex.toMap

  /**
    * The sequence of values for your [[Enum]]. You will typically want
    * to implement this in your extending class as a `val` so that `withName`
    * and friends are as efficient as possible.
    *
    * Feel free to implement this however you'd like (including messing around with ordering, etc) if that
    * fits your needs better.
   */
  def values: IndexedSeq[A]

  /**
    * Tries to get an [[A]] by the supplied name. The name corresponds to the .name
    * of the case objects implementing [[A]]
    *
    * Like [[Enumeration]]'s `withName`, this method will throw if the name does not match any of the values'
    * .entryName values.
    */
  def withName(name: String): A =
    withNameOption(name) getOrElse
      (throw new NoSuchElementException(buildNotFoundMessage(name)))

  /**
   * Optionally returns an [[A]] for a given name.
   */
  def withNameOption(name: String): Option[A] = namesToValuesMap get name

  /**
    * Tries to get an [[A]] by the supplied name. The name corresponds to the .name
    * of the case objects implementing [[A]], disregarding case
    *
    * Like [[Enumeration]]'s `withName`, this method will throw if the name does not match any of the values'
    * .entryName values.
   */
  def withNameInsensitive(name: String): A =
    withNameInsensitiveOption(name) getOrElse
      (throw new NoSuchElementException(buildNotFoundMessage(name)))

  /**
    * Optionally returns an [[A]] for a given name, disregarding case
   */
  def withNameInsensitiveOption(name: String): Option[A] = lowerCaseNamesToValuesMap get name.toLowerCase

  /**
    * Returns the index number of the member passed in the values picked up by this enum
   *
    * @param member the member you want to check the index of
    * @return the index of the first element of values that is equal (as determined by ==) to member, or -1, if none exists.
   */
  def indexOf(member: A): Int = valuesToIndex.getOrElse(member, -1)

  /**
    * Method that returns a Seq of [[A]] objects that the macro was able to find.
   *
    * You will want to use this in some way to implement your [[values]] method. In fact,
    * if you aren't using this method...why are you even bothering with this lib?
   */
  protected def findValues: IndexedSeq[A] = macro EnumMacros.findValuesImpl[A]

  private def buildNotFoundMessage(notFoundName: String): String = {
    val existingEntries = values.map(_.entryName).mkString(", ")
    s"$notFoundName is not a member of Enum ($existingEntries)"
  }

}