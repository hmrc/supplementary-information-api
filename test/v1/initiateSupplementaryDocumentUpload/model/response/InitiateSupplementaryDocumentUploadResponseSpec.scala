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

class InitiateSupplementaryDocumentUploadResponseSpec extends UnitSpec {

  "InitiateSupplementaryDocumentUploadResponse" when {
    "read from downstream JSON" should {
      "rename the reference and upload URL while preserving all fields" in {
        downstreamResponseJson.validate[InitiateSupplementaryDocumentUploadResponse] shouldBe JsSuccess(responseModel)
      }

      "reject an empty fields object" in {
        val json = downstreamResponseJson + ("uploadRequest" -> Json.obj("href" -> "https://example.com/upload", "fields" -> Json.obj()))

        json.validate[InitiateSupplementaryDocumentUploadResponse] shouldBe a[JsError]
      }

      "reject missing required fields" in {
        val uploadRequest = (downstreamResponseJson \ "uploadRequest").as[play.api.libs.json.JsObject]
        val invalidResponses = Seq(
          downstreamResponseJson - "reference",
          downstreamResponseJson - "uploadRequest",
          downstreamResponseJson + ("uploadRequest" -> (uploadRequest - "href")),
          downstreamResponseJson + ("uploadRequest" -> (uploadRequest - "fields"))
        )

        invalidResponses.foreach { json =>
          json.validate[InitiateSupplementaryDocumentUploadResponse] shouldBe a[JsError]
        }
      }

      "reject fields with non-string values" in {
        val json = downstreamResponseJson + ("uploadRequest" -> Json.obj(
          "href"   -> "https://example.com/upload",
          "fields" -> Json.obj("key" -> 123)
        ))

        json.validate[InitiateSupplementaryDocumentUploadResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(responseModel) shouldBe mtdResponseJson
      }
    }

  }

}
