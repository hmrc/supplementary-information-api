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

class UpscanCallbackSuccessRequestBodySpec extends UnitSpec {

  val json: JsValue = Json.parse(
    """
      |{
      |  "reference": "11370e18-6e24-453e-b45a-76d3e32ea33d",
      |  "downloadUrl": "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
      |  "fileStatus": "READY",
      |  "uploadDetails": {
      |    "fileName": "test.pdf",
      |    "fileMimeType": "application/pdf",
      |    "uploadTimestamp": "2026-11-19T14:49:55.260Z",
      |    "checksum": "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
      |    "size": 1024
      |  }
      |}
      |""".stripMargin
  )

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |  "reference": 123,
      |  "downloadUrl": true,
      |  "fileStatus": 456,
      |  "uploadDetails": {
      |    "fileName": 789,
      |    "fileMimeType": false,
      |    "uploadTimestamp": 123,
      |    "checksum": true,
      |    "size": "invalid"
      |  }
      |}
      |""".stripMargin
  )

  val upscanCallbackSuccessRequestBody: UpscanCallbackSuccessRequestBody =
    UpscanCallbackSuccessRequestBody(
      reference = "11370e18-6e24-453e-b45a-76d3e32ea33d",
      downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
      fileStatus = "READY",
      uploadDetails = UploadDetails(
        fileName = "test.pdf",
        fileMimeType = "application/pdf",
        uploadTimestamp = "2026-11-19T14:49:55.260Z",
        checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
        size = 1024
      )
    )

  "UpscanCallbackSuccessRequestBody" when {
    "read from valid Json" should {
      "produce the expected object" in {
        json.as[UpscanCallbackSuccessRequestBody] shouldBe upscanCallbackSuccessRequestBody
      }
    }

    "read from invalid Json" should {
      "produce a JsError" in {
        invalidJson.validate[UpscanCallbackSuccessRequestBody] shouldBe a[JsError]
      }
    }

    "read from Json with missing uploadDetails" should {
      "produce a JsError" in {
        val missingUploadDetailsJson = Json.obj(
          "reference"   -> "11370e18-6e24-453e-b45a-76d3e32ea33d",
          "downloadUrl" -> "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
          "fileStatus"  -> "READY"
        )

        missingUploadDetailsJson.validate[UpscanCallbackSuccessRequestBody] shouldBe a[JsError]
      }
    }
  }

}
