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

package v1.initiateSupplementaryDocumentUpload.model.request

import api.utils.UnitSpec
import play.api.libs.json.{JsBoolean, JsError, JsNumber, Json}
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadFixtures.*

class InitiateSupplementaryDocumentUploadRequestBodySpec extends UnitSpec {

  "InitiateSupplementaryDocumentUploadRequestBody" when {
    "read from valid JSON" should {
      "produce the expected object" when {
        "all fields present" in {
          mtdRequestBodyJson.as[InitiateSupplementaryDocumentUploadRequestBody] shouldBe requestBodyModel
        }
        "only successRedirect present" in {
          (mtdRequestBodyJson - "errorRedirect").as[InitiateSupplementaryDocumentUploadRequestBody] shouldBe
            requestBodyModel.copy(errorRedirect = None)
        }
        "only errorRedirect present" in {
          (mtdRequestBodyJson - "successRedirect").as[InitiateSupplementaryDocumentUploadRequestBody] shouldBe
            requestBodyModel.copy(successRedirect = None)

        }
        "both fields not present" in {
          Json.obj().as[InitiateSupplementaryDocumentUploadRequestBody] shouldBe
            InitiateSupplementaryDocumentUploadRequestBody(None, None)
        }

      }

    }
    "read from invalid JSON" should {
      "provide a JsError" in {
        val invalidJson = Json.obj(
          "successRedirect" -> JsNumber(123),
          "errorRedirect"   -> JsBoolean(true)
        )
        invalidJson.validate[InitiateSupplementaryDocumentUploadRequestBody] shouldBe a[JsError]
      }
    }
  }

}
