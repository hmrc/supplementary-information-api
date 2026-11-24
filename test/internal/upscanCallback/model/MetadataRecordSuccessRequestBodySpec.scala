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

package internal.upscanCallback.model

import api.utils.UnitSpec
import play.api.libs.json.{JsError, JsValue, Json}

class MetadataRecordSuccessRequestBodySpec extends UnitSpec {

  val json: JsValue = Json.parse(
    """
      |{
      |  "scanId": "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
      |  "lastModifiedTimestamp": "2026-11-19T14:49:55.260Z",
      |  "fileName": "metadata.pdf",
      |  "fileSize": 1024,
      |  "status": "requested"
      |}
      |""".stripMargin
  )

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |  "scanId": 123,
      |  "lastModifiedTimestamp": true,
      |  "fileName": 456,
      |  "fileSize": "invalid",
      |  "status": false
      |}
      |""".stripMargin
  )

  val metadataRecordSuccessRequestBody: MetadataRecordSuccessRequestBody =
    MetadataRecordSuccessRequestBody(
      scanId = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
      lastModifiedTimestamp = "2026-11-19T14:49:55.260Z",
      fileName = "metadata.pdf",
      fileSize = 1024,
      status = "requested"
    )

  "MetadataRecordSuccessRequestBody" when {
    "read from valid Json" should {
      "produce the expected object" in {
        json.as[MetadataRecordSuccessRequestBody] shouldBe metadataRecordSuccessRequestBody
      }
    }

    "read from invalid Json" should {
      "produce a JsError" in {
        invalidJson.validate[MetadataRecordSuccessRequestBody] shouldBe a[JsError]
      }
    }

    "written to Json" should {
      "produce the expected JsObject" in {
        Json.toJson(metadataRecordSuccessRequestBody) shouldBe json
      }
    }
  }

}
