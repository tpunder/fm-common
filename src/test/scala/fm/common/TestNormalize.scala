package fm.common

import org.scalatest.FunSuite
import org.scalatest.Matchers

class TestNormalize extends FunSuite with Matchers {
  
  test("lowerAlphanumericWithSpaces") {
    def t(pair: (String,String)) {
      val (str, urlName) = pair
      Normalize.lowerAlphanumericWithSpaces(str) should equal(urlName)
    }
    
    t("Foo" -> "foo")
    t("  Foo  " -> "foo")
    t(" - Foo - " -> "foo")
    t("foo BAR" -> "foo bar")
    t("  foo  BAR  " -> "foo bar")
    t("Dorman HELP!" -> "dorman help")
    t("Dorman HELP\\!" -> "dorman help")
  }
  
  test("lowerAlphanumeric") {
    def t(pair: (String,String)) {
      val (str, urlName) = pair
      Normalize.lowerAlphanumeric(str) should equal(urlName)
    }
    
    t("" -> "")
    t("Foo" -> "foo")
    t("  Foo  " -> "foo")
    t(" - Foo - " -> "foo")
    t("foo BAR" -> "foobar")
    t("  foo  BAR  " -> "foobar")
    t("Dorman HELP!" -> "dormanhelp")
    t("Dorman HELP\\!" -> "dormanhelp")
    t("dorman HELP\\!" -> "dormanhelp")
    t("doRman HELP\\!" -> "dormanhelp")
  }
  
  test("lowerAlphanumeric - Already Normalized - Should eq the original string") {
    def t(str: String) {
      Normalize.lowerAlphanumeric(str) should be theSameInstanceAs(str)
    }
    
    t("")
    t("foo")
    t("dormanhelp")
    t("dorman123help")
  }
  
  test("reverseLowerAlphanumeric") {
    Normalize.reverseLowerAlphanumeric("Foo B.O.S.C.H. Bar", "bosch") should equal (Some("B.O.S.C.H."))
    Normalize.reverseLowerAlphanumeric("FooB.O.S.C.H.Bar", "bosch") should equal (Some("B.O.S.C.H."))
    Normalize.reverseLowerAlphanumeric("B.O.S.C.H. Bar", "bosch") should equal (Some("B.O.S.C.H."))
    Normalize.reverseLowerAlphanumeric("B.O.S.C.H.Bar", "bosch") should equal (Some("B.O.S.C.H."))
    Normalize.reverseLowerAlphanumeric("Foo B.O.S.C.H.", "bosch") should equal (Some("B.O.S.C.H."))
    Normalize.reverseLowerAlphanumeric("FooB.O.S.C.H.", "bosch") should equal (Some("B.O.S.C.H."))
    Normalize.reverseLowerAlphanumeric("FooB.O.S.C.H. ", "bosch") should equal (Some("B.O.S.C.H."))
    Normalize.reverseLowerAlphanumeric("B.O.S.C.H.", "bosch") should equal (Some("B.O.S.C.H."))
    Normalize.reverseLowerAlphanumeric(" B.O.S.C.H. ", "bosch") should equal (Some("B.O.S.C.H."))
    
    Normalize.reverseLowerAlphanumeric("5S1988", "5s1988") should equal (Some("5S1988"))
    Normalize.reverseLowerAlphanumeric("AIR5S1988", "5s1988") should equal (Some("5S1988"))
    Normalize.reverseLowerAlphanumeric("5S1988AIR", "5s1988") should equal (Some("5S1988"))
    
    Normalize.reverseLowerAlphanumeric("BOSCH .....", "bosch") should equal (Some("BOSCH"))
    Normalize.reverseLowerAlphanumeric("BOSCH -", "bosch") should equal (Some("BOSCH"))
    Normalize.reverseLowerAlphanumeric(" BOSCH - ", "bosch") should equal (Some("BOSCH"))
    Normalize.reverseLowerAlphanumeric(" - BOSCH - ", "bosch") should equal (Some("BOSCH"))
  }
}
