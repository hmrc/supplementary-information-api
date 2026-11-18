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

import internal.upscanCallback.model.{FailureDetails, UploadDetails, UpscanCallbackFailureRequestBody, UpscanCallbackSuccessRequestBody}
import play.api.libs.json.{JsValue, Json}

object UpscanCallbackFixtures {

  val successJson: JsValue = Json.parse(
    """
      |{
      |  "reference": "11370e18-6e24-453e-b45a-76d3e32ea33d",
      |  "downloadUrl": "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
      |  "fileStatus": "READY",
      |  "uploadDetails": {
      |    "fileName": "test.pdf",
      |    "fileMimeType": "application/pdf",
      |    "uploadTimestamp": "2026-11-18T00:00:00Z",
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

  val successRequestBody = UpscanCallbackSuccessRequestBody(
    reference = "11370e18-6e24-453e-b45a-76d3e32ea33d",
    downloadUrl = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
    fileStatus = "READY",
    uploadDetails = UploadDetails(
      fileName = "test.pdf",
      fileMimeType = "application/pdf",
      uploadTimestamp = "2026-11-18T00:00:00Z",
      checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
      size = 1024
    )
  )

  val failureRequestBody = UpscanCallbackFailureRequestBody(
    reference = "11370e18-6e24-453e-b45a-76d3e32ea33d",
    fileStatus = "FAILED",
    failureDetails = FailureDetails(
      failureReason = "QUARANTINE",
      message = "This file has a virus"
    )
  )

}
