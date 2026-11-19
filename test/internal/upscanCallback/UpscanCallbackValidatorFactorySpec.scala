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

package internal.upscanCallback

import api.models.errors.*
import api.utils.UnitSpec
import internal.upscanCallback.UpscanCallbackFixtures.{failureJson, failureRequestBody, successJson, successRequestBody}
import internal.upscanCallback.model.*
import play.api.libs.json.*

class UpscanCallbackValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "someCorrelationId"

  private val validatorFactory = new UpscanCallbackValidatorFactory

  private def validate(body: JsValue = successJson) =
    validatorFactory.validator(body).validateAndWrapResult()

  private def error(mtdError: MtdError) =
    Left(ErrorWrapper(correlationId, mtdError))

  "UpscanCallbackValidatorFactory" should {
    "validator" when {
      "a valid READY callback request is supplied" in {
        validate(successJson) shouldBe Right(
          UpscanCallbackRequestData(successRequestBody)
        )
      }

      "a valid FAILED callback request is supplied" in {
        validate(failureJson) shouldBe Right(
          UpscanCallbackRequestData(failureRequestBody)
        )
      }
    }

    "return a RuleIncorrectOrEmptyBodyError" when {
      "an empty JSON body is submitted" in {
        validate(JsObject.empty) shouldBe error(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without expected fields" in {
        validate(Json.parse("""{"field": "value"}""")) shouldBe
          error(RuleIncorrectOrEmptyBodyError.withPath("/fileStatus"))
      }

      "the READY callback has missing uploadDetails" in {
        val invalidJson = successJson.as[JsObject] - "uploadDetails"

        validate(invalidJson) shouldBe
          error(RuleIncorrectOrEmptyBodyError.withPath("/uploadDetails"))
      }

      "the FAILED callback has missing failureDetails" in {
        val invalidJson = failureJson.as[JsObject] - "failureDetails"

        validate(invalidJson) shouldBe
          error(RuleIncorrectOrEmptyBodyError.withPath("/failureDetails"))
      }

      "an unsupported fileStatus is supplied" in {
        val invalidJson = successJson.as[JsObject] + (
          "fileStatus",
          JsString("UNKNOWN")
        )

        validate(invalidJson) shouldBe
          error(RuleIncorrectOrEmptyBodyError.withPath("/fileStatus"))
      }
    }
  }

}
