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

object IPSubnet {
  val Localhost: IPSubnet = parse("127.0.0.0/8")
  
  def parse(subnet: String): IPSubnet = {
    val slashes: Int = subnet.countOccurrences('/')
    val dashes: Int = subnet.countOccurrences('-')
    
    if (slashes == 1) {
      val Array(ip, bits) = subnet.split('/')
      apply(IP(ip), bits.toInt)
    } else if (dashes == 1) {
      val Array(from, to) = subnet.split('-')
      forRangeOrMask(IP(from), IP(to))
    } else throw new IllegalArgumentException("Not sure how to parse subnet: "+subnet)
  }
  
  def forRangeOrMask(from: IP, toOrMask: IP): IPSubnet = {
    val validMask: Boolean = isValidMask(toOrMask)
    val validRange: Boolean = isValidRange(from, toOrMask)
    
    require(validMask || validRange, "Not a valid range or mask")
    require(!(validMask && validRange), "toOrMask parameter is ambiguous since it looks like both a valid mask and valid range")
    
    if (validMask) forMask(from, toOrMask)
    else if (validRange) forRange(from, toOrMask)
    else throw new Exception("Invalid Condition")
  }
  
  def forMask(ip: IP, mask: IP): IPSubnet = {
    require(isValidMask(mask), "Not a valid mask: "+mask)
    val leadingOnes: Int = numberOfLeadingOnes(mask.intValue)
    apply(ip, leadingOnes)
  }
  
  def forRange(from: IP, to: IP): IPSubnet = {
    require(from < to, s"""Expected from "$from" to be less than to "$to"""")
    apply(from, commonPrefixBitCount(from.intValue, to.intValue))
  }
  
  //def apply(ip: IP, bits: Int): IPSubnet = IPSubnet(ip.intValue, bits)
  
  def isValidMask(ip: IP): Boolean = isValidMaskImpl(ip.intValue)
  
  /**
   * Could this be a valid bit mask?  Should be leading ones and trailing zeros
   */
  private def isValidMaskImpl(ip: Int): Boolean = {
    val trailingZeros: Int = Integer.numberOfTrailingZeros(ip)
    val leadingOnes: Int = numberOfLeadingOnes(ip)
    trailingZeros + leadingOnes == 32
  }
  
  def isValidRange(from: IP, to: IP): Boolean = isValidRangeImpl(from.intValue, to.intValue)
  
  /**
   * Could this be a valid subnet range?
   * 
   *   - from < to
   *   - They should have a common prefix and all bits after the prefix in from should be zero and in to should be 1
   */
  private def isValidRangeImpl(from: Int, to: Int): Boolean = {
    val commonPrefix: Int = commonPrefixBitCount(from, to)
    val fromTrailingZeros: Int = Integer.numberOfTrailingZeros(from)
    val toTrailingOnes: Int = numberOfTrailingOnes(to)
    
    val validFrom: Boolean = commonPrefix + fromTrailingZeros >= 32
    val validTo: Boolean = commonPrefix + toTrailingOnes >= 32
    
    from < to && validFrom && validTo 
  }
  
  private def commonPrefixBitCount(a: Int, b: Int): Int = Integer.numberOfLeadingZeros(a ^ b)
  private def numberOfLeadingOnes(i: Int): Int = Integer.numberOfLeadingZeros(i ^ 0xffffffff)
  private def numberOfTrailingOnes(i: Int): Int = Integer.numberOfTrailingZeros(i ^ 0xffffffff)
}

final case class IPSubnet(ip: IP, bits: Int) {
  require(bits <= 32, "Invalid bits: "+bits)
  
  private[this] val shift: Int = 32 - bits
  private[this] val prefix: Int = ip.intValue >>> shift

  /**
   * Does this Subnet contain the IP address?
   */
  def contains(other: IP): Boolean = prefix == other.longValue >>> shift

  /**
   * The starting IP address for this Subnet
   */
  def start: IP = ip
  
  /**
   * The ending IP address for this subnet
   */
  def end: IP = IP(ip.intValue | ((0xffffffff << shift) ^ 0xffffffff))
  
  override def toString = ip.toString+"/"+bits
}