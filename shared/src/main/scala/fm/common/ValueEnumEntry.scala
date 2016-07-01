/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 by Lloyd Chan
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
 * Created by Lloyd on 4/11/16.
 *
 * Copyright 2016
 */

sealed trait ValueEnumEntry[ValueType <: AnyVal] {

  /**
   * Value of this entry
   */
  def value: ValueType

}

/**
 * Value Enum Entry parent class for [[Int]] valued entries
 */
abstract class IntEnumEntry extends ValueEnumEntry[Int]

/**
 * Value Enum Entry parent class for [[Long]] valued entries
 */
abstract class LongEnumEntry extends ValueEnumEntry[Long]

/**
 * Value Enum Entry parent class for [[Short]] valued entries
 */
abstract class ShortEnumEntry extends ValueEnumEntry[Short]