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

package v1.retrieveSupplementaryInformation.model.response

import api.utils.UnitSpec
import play.api.libs.json.{JsError, JsValue, Json}
import v1.retrieveSupplementaryInformation.RetrieveSupplementaryInformationFixtures.*

class RetrieveSupplementaryInformationResponseSpec extends UnitSpec {

  private val missingWhiteSpaceJson: JsValue = Json.parse("""{"submittedOn": "2026-08-24T14:15:22Z"}""")
  private val emptyJson: JsValue             = Json.parse("""{}""")

  "RetrieveSupplementaryInformationResponse" when {
    "read from valid downstream JSON" should {
      "produce the expected object" in {
        downstreamResponseJson.as[RetrieveSupplementaryInformationResponse](RetrieveSupplementaryInformationResponse.reads) shouldBe responseModel
      }
    }

    "read from downstream JSON without whiteSpace" should {
      "provide a JsError" in {
        missingWhiteSpaceJson
          .validate[RetrieveSupplementaryInformationResponse](RetrieveSupplementaryInformationResponse.reads) shouldBe a[JsError]
      }
    }

    "read from empty downstream JSON" should {
      "provide a JsError" in {
        emptyJson.validate[RetrieveSupplementaryInformationResponse](RetrieveSupplementaryInformationResponse.reads) shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected MTD JsObject" in {
        Json.toJson(responseModel) shouldBe mtdResponseJson
      }
    }
  }

  "RetrieveSupplementaryInformationResponse" when {
    "written to JSON via the trait" should {
      "produce the expected MTD JsObject" in {
        Json.toJson[RetrieveSupplementaryInformationResponse](responseModel) shouldBe mtdResponseJson
      }
    }
  }

}
