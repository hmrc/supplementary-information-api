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

package v1.createAmend.def1

import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.utils.JsonErrorValidators
import api.utils.UnitSpec
import play.api.libs.json.*
import v1.createAmend.def1.fixture.Def1_CreateAmendSupplementaryInformationFixture.*
import v1.createAmend.def1.model.request.Def1_CreateAmendSupplementaryInformationRequestData

class Def1_CreateAmendSupplementaryInformationValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "someCorrelationId"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2026-27"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def validate(nino: String = validNino, body: JsValue = requestBodyJson) =
    new Def1_CreateAmendSupplementaryInformationValidator(nino, validTaxYear, body).validateAndWrapResult()

  private def error(mtdError: MtdError) = Left(ErrorWrapper(correlationId, mtdError))

  "Def1_CreateAmendSupplementaryInformationValidator" should {
    "return the parsed object" when {
      "return the parsed object" when {
        "a valid request is supplied" in {
          validate(validNino, requestBodyJson) shouldBe
            Right(
              Def1_CreateAmendSupplementaryInformationRequestData(
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
                Def1_CreateAmendSupplementaryInformationRequestData(
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

    "return FORMAT_SUPPLEMENTARY_INFORMATION" when {
      val invalidCharSample = """:|%\""".map(char => (char.toString, s"with special char $char"))
      val invalidLengths    = Seq(0, 4001).map(length => ("a" * length, s"with length $length"))

      val tests = invalidCharSample ++ invalidLengths

      tests.foreach { (value, description) =>
        description in {
          val validJson: JsValue = requestBodyJson.update("/supplementaryInformation", JsString(value))

          validate(body = validJson) shouldBe error(FormatSupplementaryInformationError)
        }
      }
    }
  }

}
