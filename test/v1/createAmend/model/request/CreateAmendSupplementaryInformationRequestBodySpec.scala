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

package v1.createAmend.model.request

import api.utils.UnitSpec
import play.api.libs.json.{JsError, JsValue, Json}
import v1.createAmend.CreateAmendSupplementaryInformationFixtures.*

class CreateAmendSupplementaryInformationRequestBodySpec extends UnitSpec {

  val invalidJson: JsValue = Json.parse(
    """
      |{}
      |""".stripMargin
  )

  "CreateAmendSupplementaryInformationRequestBody" when {
    "read from valid Json" should {
      "produce the expected object" in {
        mtdRequestBodyJson.as[CreateAmendSupplementaryInformationRequestBody] shouldBe requestBodyModel
      }
    }

    "read from invalid Json" should {
      "provide a JsError" in {
        invalidJson.validate[CreateAmendSupplementaryInformationRequestBody] shouldBe a[JsError]
      }
    }

    "written to Json" should {
      "produce the expected JsObject" in {
        Json.toJson(requestBodyModel) shouldBe downstreamRequestBodyJson
      }
    }
  }

}
