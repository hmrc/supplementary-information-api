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

package v1.initiateSupplementaryDocumentUpload.model.response

import api.utils.UnitSpec
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadFixtures.*

class UploadRequestSpec extends UnitSpec {

  private val downstreamJson = (downstreamResponseJson \ "uploadRequest").as[JsObject]

  "UploadRequest" when {
    "read from downstream JSON" should {
      "rename the upload URL while preserving all fields" in {
        downstreamJson.validate[UploadRequest] shouldBe JsSuccess(responseModel.uploadRequest)
      }

      "return a JsError" when {
        "there are missing fields" in {
          val invalidRequests = Seq(
            downstreamJson - "href",
            downstreamJson - "fields"
          )

          invalidRequests.foreach { json =>
            json.validate[UploadRequest] shouldBe a[JsError]
          }
        }

        "fields object is empty" in {
          val json = downstreamJson + ("fields" -> Json.obj())
          json.validate[UploadRequest] shouldBe a[JsError]
        }

        "fields is not a string" in {
          val json = downstreamJson + ("fields" -> Json.obj("key" -> 123))
          json.validate[UploadRequest] shouldBe a[JsError]
        }
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(responseModel.uploadRequest) shouldBe (mtdResponseJson \ "uploadRequest").as[JsObject]
      }
    }
  }

}
