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

import org.scalatest.{FunSuite, Matchers}

class TestMessageCrypto extends FunSuite with Matchers {
  test("Basic Encryption Key Sizes") {
   
    // Should use sha256 hash
    encrypt("", "Hello World")
    encrypt("a", "Hello World")
    encrypt("abc", "Hello World")

    // Should use key exactly
    encrypt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Hello World")
    
    // Should truncate
    encrypt("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01", "Hello World")
    encrypt("121021ec251fdb27b442ddba4847b92c4bb71594071a2b7746e2ecf8beea0223", "Hello World")
  }

  test("Encryption Keys of various sizes") {
    var key = ""
    (0 to 1024).foreach{i =>
      encrypt(key, "Hello World")
      key += "a"
    }
  }

  test("Encryption/Decryption of various sizes") {
    val key = "dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01"

    var s = ""

    (0 to 1024).foreach{ i =>
      encrypt(key, s)
      s += "a"
    }
  }

  test("Basic Signing") {
    sign("", "Hello World")
    sign("a", "Hello World")
    sign("abc", "Hello World")
    sign("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Hello World")
    sign("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01", "Hello World")
    sign("121021ec251fdb27b442ddba4847b92c4bb71594071a2b7746e2ecf8beea0223", "Hello World")
  }

  test("Basic encryptAndSign") {
    encryptAndSign("", "Hello World")
    encryptAndSign("a", "Hello World")
    encryptAndSign("abc", "Hello World")
    encryptAndSign("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Hello World")
    encryptAndSign("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01", "Hello World")
    encryptAndSign("121021ec251fdb27b442ddba4847b92c4bb71594071a2b7746e2ecf8beea0223", "Hello World")
  }

  test("Rails ActiveSupport::MessageEncryptor.encrypt Key Size Interop") {
    def check(key:String, plaintext:String, ciphertext:String) {
      val c = MessageCrypto(key)
      c.decrypt(ciphertext) should equal(plaintext)
    }

    // This is the min key length for ruby/openssl aes-256
    check("dce104043477fa295bb97c509d6fb662", "Foo", "UnYLROXGqV7nDpRCBVPkNQ==--rAqJjP0keNH/Vt+9ClWcmA==")

    // This should get truncated down to 256 bits
    check("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01", "Foo", "H5b/Taz3jygpGJVmZQE8iw==--N5i+j1ldI8saCbUAmE04yA==")
  }

  test("Rails ActiveSupport::MessageEncryptor.encrypt Interop") {
    val c = MessageCrypto("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01")
    c.decrypt("idtnXxSZKDiJoyk0Kif9jQ==--6FghjILVom+lscED4e49Lw==") should equal("")
    c.decrypt("UsjXEqQ7uKgsJZULlOEpPA==--0iRba2OjgbRmGm83oKQvVg==") should equal("Foo")
    c.decrypt("Yoll1jeJnZqxnA8HffFLCA==--qLyl9W+QsRcgisvKXKeoiQ==") should equal("Hello World")
    c.decrypt("CPoSoK2gEAAQpthF1rhqv4iwnl75x+ceOxZZUKQVrla+jszJoKJ0k8vv/DWkZB2QlZMOuKEj8AdRgYdpMDW+dZDHok7LEaP1PH8DhuKEZdP2oifyjWTEwzQfM7byKA/t9XfxUW5EF9uEN8faHesa2rv4K2RDb0YOBrlS1uwjPK9g4J+IiJVdPpjLB0DNfbmE--/43vtO2gkWy9t/6/oRn59g==") should equal("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01")
    c.decrypt("c4iLqTAIQmOTKIrK5lZ8rwTqi9R/x3feYGSSvhzuQO3ojWjTY21ZWE6VFCMlEW42XfLmaoIMaKVw8KSx9x7Qiz/9DEqounq7ChOg8ApIV9eFA7Pnkwm3iMvFkjCLEPtd7WGoNL7uGxoPPdpLHwKDN2blaxQC0+ZDALWAfNSO/tbnXEdBNG+YbOEOHvZtqWV7lrlA7NT4E59CLnT6UWQr8CyrAYpJ+O5QJku6ANlPgt1onwFGIQT3XZsAJaoEUvQn+uwqw3oH3XCBHpvHLW6ybUPueTf7agf5m+BA7kdaw6dA10ULLljNecK24fzh305CQ+DtZ+YNe22J4869m6sXBISuCQ0hnd8Eosq1Lli0Sn8VEhVMIK8cspRfjwcLnnOCSE/EWOSdWxZTdga8p5VVCVuEoOmBGADPzY8UtrWcbRCJp2dgcHXMXSNLERipmzH8Tj8GnBmRhG3bSgQBZq5qXFRJARc4uRThDyPOvwjrC8Kv+6HxeUKfIZd73E0XgrJlTJraX15JbfYkts0qL7kwwg==--VHN7+520e6EmMueP6bR6iw==") should equal("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01")
  }

  test("FM's MessageCrypto.encrypt Interop") {
    val c = MessageCrypto("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01", json=true)
    c.decrypt("HNUjCKVbV1Xv2I3Bt5ADfQ==--uXGLQL2SFcSIWvKHBxAWPA==") should equal("")
    c.decrypt("bZDOp79u5zxWz1EVHuejyQ==--HUeE8TcsS4TXsiVfdwN2Tw==") should equal("Foo")
    c.decrypt("vBDAnQE60cLsmNDbBjqn7A==--IKhD8K0FQIZc+hS4cBR8gA==") should equal("Hello World")
    c.decrypt("G0kH4Bn+b8Gdh/0yCWG7vCNZr/56FsInRMpT9eao9KjmpeZf1ff/vtN9lzWp0/B8mBo3tsyERgn/TEEyEbvOt+821zHsLjxTIj86Y2wGXl8nJx4vtU6v5xW3yomBPAYm7/n8Rbq75N4fBHUDGkxCNG3Nwnfzo4hbyIzE4HClsIAowAFTHeGpK3IR2dF2ck1M--XiUSdo4wQr8sfgih1iO0RQ==") should equal("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01")
    c.decrypt("sx79iA3z+JM5yLq4ur0Zy5qBro9Rf41S/NyBbPEzKsQKwKiH5iAffWNoUshWdHiJ8gWUnbOtkg/PCAKa6UYQ5za765xhr8oWbXbdPT/BteVSJwzCVE/8jn+FCZPAgg/Z7xy+7agvMxnVxr2np2lABhojWhHpemJOARFLKiqCHnytKmaHIumZB0sZ4LuE/V5CQR0pmpIDyTQBAQrAJUIRTyTIjJOE9fNg0FIOAtdVggh1tNGrEvjjnCDpDlCWWcnfiRRMXghkRx0hCmhAl8XCNaitFSrXJNVo9GV8nXygckjEVYwe1M+m6W3JC3WKegYLA5jPTHDwRMNibImWcFKU6JyhWm3H/rgI+4nvsE9wRiHHfDNSGRAMCjWtWsvY5Re/Ev63RY5568PcF9E53JoWMAuAA8yRb50HTMDRWlmKZZKH3wiXXYawIG+stuS0NNzh2XKsTGFXj+sq7fl1JadsUw6KCRSJVwycJ9MY2Hc6UiDkUmN4AF6M2NpO5eVRT20UYfjSCP+WfwaLAj1fVoR7dA==--eD3kYAMMlaiVPkXXs2HQFA==") should equal("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01")
  }

  test("Rails ActiveSupport::MessageVerifier.generate Key Size Interop") {
    def check(key:String, plaintext:String, signed:String) {
      val c = MessageCrypto(key)
      c.verify(signed) should equal(Some(plaintext))
    }

    check("", "Foo", "BAgiCEZvbw==--9b3e21d3a78121d2847de57d39f5263cb29e45cb")
    check("a", "Foo", "BAgiCEZvbw==--0943c2c7823b436b3f9bb13eedaf9b0ed8c54d91")
    check("dce104043477fa295bb97c509d6fb662", "Foo", "BAgiCEZvbw==--91a5ac856d7d9d3ce55d2c6a0988dbff71af4343")
    check("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01", "Foo", "BAgiCEZvbw==--03737b299a95ce45194e9bf286f21c29a6898eff")
  }

  test("Rails ActiveSupport::MessageVerifier.generate Interop") {
    val c = MessageCrypto("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01")

    def verify(signed:String, plain:String) {
      c.sign(plain) should equal(signed)
      c.verify(signed) should equal(Some(plain))
    }

    verify("BAgiAA==--008d8aecfaef43ba5a93ef49c9e491f3e8b33412", "")
    verify("BAgiCEZvbw==--03737b299a95ce45194e9bf286f21c29a6898eff", "Foo")
    verify("BAgiEEhlbGxvIFdvcmxk--c5e02284b487bb5959930debb8389f9e7f6d89af", "Hello World")
    verify("BAgiAYBkY2UxMDQwNDM0NzdmYTI5NWJiOTdjNTA5ZDZmYjY2MmE5ZGVhZDNmOTQzZDY0NTgwZjNjZTc4ZTFlYzIyYzAxZGNlMTA0MDQzNDc3ZmEyOTViYjk3YzUwOWQ2ZmI2NjJhOWRlYWQzZjk0M2Q2NDU4MGYzY2U3OGUxZWMyMmMwMQ==--777b7b7fe0dcd4f6d30b83ef7e09db0c8280863a", "dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01")
  }

  test("FM's MessageCrypto.generate Interop") {
    val c = MessageCrypto("dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01",json=true)

    def verify(signed:String, plain:String) {
      c.sign(plain) should equal(signed)
      c.verify(signed) should equal(Some(plain))
    }

    verify("IiI=--654674a965da804170bab7aeaf6cc3a920961f3f", "")
    verify("IkZvbyI=--95fc46708e826158a6d0d1044cc12a2c111e2cdd", "Foo")
    verify("IkhlbGxvIFdvcmxkIg==--f6a22a9b3dce60af58f2c52393daeffba46ca95b", "Hello World")
    verify("ImRjZTEwNDA0MzQ3N2ZhMjk1YmI5N2M1MDlkNmZiNjYyYTlkZWFkM2Y5NDNkNjQ1ODBmM2NlNzhlMWVjMjJjMDFkY2UxMDQwNDM0NzdmYTI5NWJiOTdjNTA5ZDZmYjY2MmE5ZGVhZDNmOTQzZDY0NTgwZjNjZTc4ZTFlYzIyYzAxIg==--68cc804a11e3649e0eab0e77df843aa9790fadcb", "dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01dce104043477fa295bb97c509d6fb662a9dead3f943d64580f3ce78e1ec22c01")
  }

  def encryptAndSign(key:String, msg:String) {
    List(MessageCrypto(key), MessageCrypto(key,json=true)).foreach{ c =>
      val ciphertext = c.encryptAndSign(msg)
      val plaintextOption = c.decryptAndVerify(ciphertext)
      plaintextOption should equal(Some(msg))
    }
  }

  def encrypt(key:String, msg:String) {
    List(MessageCrypto(key), MessageCrypto(key,json=true)).foreach{ c =>
      val ciphertext = c.encrypt(msg)
      val plaintext = c.decrypt(ciphertext)
      msg should equal(plaintext)
    }
  }

  def sign(key:String, msg:String) {
    List(MessageCrypto(key), MessageCrypto(key,json=true)).foreach{ c =>
      val signed = c.sign(msg)
      val res = c.verify(signed)
      res should equal (Some(msg))
    }
  }
}
