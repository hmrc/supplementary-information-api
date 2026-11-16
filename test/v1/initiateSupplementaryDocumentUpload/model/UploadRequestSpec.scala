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

package v1.initiateSupplementaryDocumentUpload.model

import api.utils.UnitSpec
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadFixtures.*
import v1.initiateSupplementaryDocumentUpload.model.response.UploadRequest

class UploadRequestSpec extends UnitSpec {

  private val downstreamJson = (downstreamResponseJson \ "uploadRequest").as[JsObject]
  private val uploadRequest  = responseModel.uploadRequest

  "UploadRequest" when {
    "read from downstream JSON" should {
      "map href to uploadUrl and preserve the upload fields" in {
        downstreamJson.validate[UploadRequest] shouldBe JsSuccess(uploadRequest)
      }

      "accept an empty fields object" in {
        val json = downstreamJson + ("fields" -> Json.obj())

        json.validate[UploadRequest] shouldBe JsSuccess(uploadRequest.copy(fields = Map.empty))
      }

      Seq("href", "fields").foreach { field =>
        s"reject a missing $field" in {
          (downstreamJson - field).validate[UploadRequest] shouldBe a[JsError]
        }
      }

      "reject a non-string href" in {
        (downstreamJson + ("href" -> Json.toJson(123))).validate[UploadRequest] shouldBe a[JsError]
      }

      "reject a fields value that is not an object" in {
        (downstreamJson + ("fields" -> Json.arr())).validate[UploadRequest] shouldBe a[JsError]
      }

      "reject fields with non-string values" in {
        (downstreamJson + ("fields" -> Json.obj("key" -> 123))).validate[UploadRequest] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "use uploadUrl instead of href and preserve the upload fields" in {
        Json.toJson(uploadRequest) shouldBe (mtdResponseJson \ "uploadRequest").as[JsObject]
      }
    }
  }

}
