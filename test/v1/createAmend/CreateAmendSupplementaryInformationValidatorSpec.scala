/*
 * Copyright 2026 HM Revenue & Customs
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

package v1.createAmend

import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.utils.JsonErrorValidators
import api.utils.UnitSpec
import play.api.libs.json.*
import v1.createAmend.CreateAmendSupplementaryInformationFixtures.*
import v1.createAmend.model.request.CreateAmendSupplementaryInformationRequestData

class CreateAmendSupplementaryInformationValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "someCorrelationId"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2026-27"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def validate(nino: String = validNino, taxYear: String = validTaxYear, body: JsValue = requestBodyJson) =
    new CreateAmendSupplementaryInformationValidator(nino, taxYear, body).validateAndWrapResult()

  private def error(mtdError: MtdError) = Left(ErrorWrapper(correlationId, mtdError))

  "CreateAmendSupplementaryInformationValidator" should {
    "return the parsed object" when {
      "return the parsed object" when {
        "a valid request is supplied" in {
          validate(validNino, validTaxYear, requestBodyJson) shouldBe
            Right(
              CreateAmendSupplementaryInformationRequestData(
                parsedNino,
                parsedTaxYear,
                requestBodyModel
              ))
        }

        "a valid supplementaryInformation field is supplied" when {
          val allowedChars   = """&'"()*,-./@£""".map(char => (char.toString, s"with special char $char"))
          val allowedLengths = Seq(1, 4000).map(length => ("a" * length, s"with length $length"))
          val alphanumeric = Seq(
            (('a' to 'z').toString, "with lowercase letters"),
            (('A' to 'A').toString, "with uppercase letters"),
            (('0' to '9').toString, "with numbers"),
            ("a b", "with space")
          )

          val tests = allowedChars ++ allowedLengths ++ alphanumeric

          tests.foreach { (value, description) =>
            description in {
              val validJson: JsValue = requestBodyJson.update("/supplementaryInformation", JsString(value))

              validate(body = validJson) shouldBe Right(
                CreateAmendSupplementaryInformationRequestData(
                  parsedNino,
                  parsedTaxYear,
                  requestBodyModel.copy(supplementaryInformation = value)
                ))
            }
          }
        }
      }
    }

    "return a NinoFormatError" when {
      "an invalid nino is supplied" in {
        validate(nino = "A12344A") shouldBe error(NinoFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "an unsupported tax year is supplied" in {
        validate(taxYear = "2025-26") shouldBe error(RuleTaxYearNotSupportedError)
      }
    }

    "return TaxYearFormatError" when {
      "the tax year format is invalid" in {
        validate(taxYear = "25-26") shouldBe error(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "the tax year range is invalid" in {
        validate(taxYear = "2025-27") shouldBe error(RuleTaxYearRangeInvalidError)
      }
    }

    "return a RuleIncorrectOrEmptyBodyError" when {
      "an empty Json body is submitted" in {
        validate(body = JsObject.empty) shouldBe error(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in {
        validate(body = Json.parse("""{"field": "value"}""")) shouldBe error(RuleIncorrectOrEmptyBodyError.withPath("/supplementaryInformation"))
      }

      requestBodyJson.collectPaths().foreach { path =>
        s"the submitted request body has field $path with incorrect type" in {
          val invalidJson: JsValue = requestBodyJson.update(path, JsBoolean(true))

          validate(body = invalidJson) shouldBe error(RuleIncorrectOrEmptyBodyError.withPath(path))
        }
      }
    }

    "return a FormatSupplementaryInformationError" when {
      val invalidCharSample     = """:|%\""".map(char => (char.toString, s"with special char $char"))
      val invalidLengths        = Seq(0, 4001).map(length => ("a" * length, s"with length $length"))
      val invalidWhitespaceOnly = Seq(("    ", "with whitespace only"))

      val tests = invalidCharSample ++ invalidLengths ++ invalidWhitespaceOnly

      tests.foreach { (value, description) =>
        description in {
          val validJson: JsValue = requestBodyJson.update("/supplementaryInformation", JsString(value))

          validate(body = validJson) shouldBe error(FormatSupplementaryInformationError)
        }
      }
    }
  }

}
