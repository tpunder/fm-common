/*
 * Copyright 2017 Frugal Mechanic (http://frugalmechanic.com)
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

import java.util.Locale
import org.scalatest.{FunSuite, Matchers}

final class TestRichLocale extends FunSuite with Matchers {
  import fm.common.Implicits._

  test("isValid / isValidLanguage / isValidCountry - Built-In Locales") {
    // Language+Country Built-In Locales
    checkLocale(Locale.CANADA)
    checkLocale(Locale.CANADA_FRENCH)
    checkLocale(Locale.CHINA)
    checkLocale(Locale.FRANCE)
    checkLocale(Locale.GERMANY)
    checkLocale(Locale.ITALY)
    checkLocale(Locale.JAPAN)
    checkLocale(Locale.KOREA)
    checkLocale(Locale.PRC)
    checkLocale(Locale.PRC)
    checkLocale(Locale.SIMPLIFIED_CHINESE)  // CN is the country: createConstant("zh", "CN");
    checkLocale(Locale.TRADITIONAL_CHINESE) // TW is the country: createConstant("zh", "TW");
    checkLocale(Locale.TAIWAN)
    checkLocale(Locale.US)
    checkLocale(Locale.UK)

    // Language Only Built-In Locales
    checkLocale(Locale.CHINESE, isValidCountry = false)
    checkLocale(Locale.ENGLISH, isValidCountry = false)
    checkLocale(Locale.FRENCH, isValidCountry = false)
    checkLocale(Locale.GERMAN, isValidCountry = false)
    checkLocale(Locale.ITALIAN, isValidCountry = false)
    checkLocale(Locale.JAPANESE, isValidCountry = false)
    checkLocale(Locale.KOREAN, isValidCountry = false)

    checkLocale(Locale.ROOT, isValid = false, isValidLanguage = false, isValidCountryOrIsBlankCountry = true, isValidCountry = false)
  }

  test("isValid / isValidLanguage / isValidCountry - Manually Constructed Locales") {
    checkLocale(new Locale("en","US"))
    checkLocale(new Locale("en","US", "foo"))

    checkLocale(new Locale("",""), isValid = false, isValidLanguage = false, isValidCountryOrIsBlankCountry = true, isValidCountry = false)
    checkLocale(new Locale("","",""), isValid = false, isValidLanguage = false, isValidCountryOrIsBlankCountry = true, isValidCountry = false)
  }

  test("isValid / isValidLanguage / isValidCountry - Custom Tags") {
    checkTag("en-US") // Should be the same as Locale.US

    checkTag("en", isValidCountry = false)
    checkTag("de", isValidCountry = false)
    checkTag("fr", isValidCountry = false)

    checkTag("en-ZZ", isValid = false, isValidCountryOrIsBlankCountry = false, isValidCountry = false)
    checkTag("de-ZZ", isValid = false, isValidCountryOrIsBlankCountry = false, isValidCountry = false)
    checkTag("fr-ZZ", isValid = false, isValidCountryOrIsBlankCountry = false, isValidCountry = false)

    checkTag("zz-ZZ", isValid = false, isValidLanguage = false, isValidCountryOrIsBlankCountry = false, isValidCountry = false)
  }

  private def checkTag(tag: String, isValid: Boolean = true, isValidLanguage: Boolean = true, isValidCountryOrIsBlankCountry: Boolean = true, isValidCountry: Boolean = true): Unit = {
    checkLocale(Locale.forLanguageTag(tag), isValid = isValid, isValidLanguage = isValidLanguage, isValidCountryOrIsBlankCountry = isValidCountryOrIsBlankCountry, isValidCountry = isValidCountry)
  }

  private def checkLocale(locale: Locale, isValid: Boolean = true, isValidLanguage: Boolean = true, isValidCountryOrIsBlankCountry: Boolean = true, isValidCountry: Boolean = true): Unit = {
    withClue(s"Locale: $locale   (Expected == Actual | isValid: $isValid == ${locale.isValid} | isValidLanguage: $isValidLanguage == ${locale.isValidLanguage} | isValidCountryOrIsBlankCountry: $isValidCountryOrIsBlankCountry == ${locale.isValidCountryOrIsBlankCountry} | isValidCountry: $isValidCountry == ${locale.isValidCountry})") {
      locale.isValid should equal (isValid)
      locale.isValidLanguage should equal (isValidLanguage)
      locale.isValidCountryOrIsBlankCountry should equal (isValidCountryOrIsBlankCountry)
      locale.isValidCountry should equal (isValidCountry)
    }
  }

}