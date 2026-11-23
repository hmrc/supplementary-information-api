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

class UpscanCallbackRequestBodySpec extends UnitSpec {

  val successJson: JsValue = Json.parse(
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

  val failureJson: JsValue = Json.parse(
    """
      |{
      |  "reference": "11370e18-6e24-453e-b45a-76d3e32ea33d",
      |  "fileStatus": "FAILED",
      |  "failureDetails": {
      |    "failureReason": "QUARANTINE",
      |    "message": "This file has a virus"
      |  }
      |}
      |""".stripMargin
  )

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |  "reference": "REF12345",
      |  "fileStatus": 123
      |}
      |""".stripMargin
  )

  "UpscanCallbackRequestBody" when {

    "read from valid READY Json" should {
      "produce the expected success object" in {
        successJson.as[UpscanCallbackRequestBody] shouldBe
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
      }
    }

    "read from valid FAILED Json" should {
      "produce the expected failure object" in {
        failureJson.as[UpscanCallbackRequestBody] shouldBe
          UpscanCallbackFailureRequestBody(
            reference = "11370e18-6e24-453e-b45a-76d3e32ea33d",
            fileStatus = "FAILED",
            failureDetails = FailureDetails(
              failureReason = "QUARANTINE",
              message = "This file has a virus"
            )
          )
      }
    }

    "read from Json with invalid fileStatus type" should {
      "produce a JsError" in {
        invalidJson.validate[UpscanCallbackRequestBody] shouldBe a[JsError]
      }
    }

    "read from READY Json with missing uploadDetails" should {
      "produce a JsError" in {
        val missingUploadDetailsJson = Json.obj(
          "reference"   -> "11370e18-6e24-453e-b45a-76d3e32ea33d",
          "fileStatus"  -> "READY",
          "downloadUrl" -> "https://bucketName.s3.eu-west-2.amazonaws.com?1235676"
        )

        missingUploadDetailsJson.validate[UpscanCallbackRequestBody] shouldBe a[JsError]
      }
    }

    "read from FAILED Json with missing failureDetails" should {
      "produce a JsError" in {
        val missingFailureDetailsJson = Json.obj(
          "reference"  -> "11370e18-6e24-453e-b45a-76d3e32ea33d",
          "fileStatus" -> "FAILED"
        )

        missingFailureDetailsJson.validate[UpscanCallbackRequestBody] shouldBe a[JsError]
      }
    }
  }

}
