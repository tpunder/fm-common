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

/**
 * Base type for an enum entry for [[Enum]]
 *
 * By default, the entryName method used for serialising and deseralising Enum values uses
 * toString, but feel free to override to fit your needs.
 *
 * Mix in the supplied stackable traits to convert the entryName to [[EnumEntry.Snakecase Snakecase]],
 * [[EnumEntry.Uppercase Uppercase]], and [[EnumEntry.Lowercase Lowercase]]
 */
abstract class EnumEntry {

  /**
   * String representation of this Enum Entry.
   *
   * Override in your implementation if needed
   */
  def entryName: String = toString

}

object EnumEntry {

  /**
   * Stackable trait to convert the entryName to snake_case. For UPPER_SNAKE_CASE,
   * also mix in [[Uppercase]] after this one.
   */
  trait Snakecase extends EnumEntry {
    abstract override def entryName: String = camel2snake(super.entryName)

    private def camel2snake(name: String) =
      "[A-Z]".r.replaceAllIn(name, { m => "_" + m.group(0).toLowerCase }).stripPrefix("_")
  }

  /**
   * Stackable trait to convert the entryName to UPPERCASE.
   */
  trait Uppercase extends EnumEntry {
    abstract override def entryName: String = super.entryName.toUpperCase
  }

  /**
   * Stackable trait to convert the entryName to lowercase.
   */
  trait Lowercase extends EnumEntry {
    abstract override def entryName: String = super.entryName.toLowerCase
  }

}
