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

class UploadDetailsSpec extends UnitSpec {

  val json: JsValue = Json.parse(
    """
      |{
      |  "fileName": "test.pdf",
      |  "fileMimeType": "application/pdf",
      |  "uploadTimestamp": "2026-11-19T14:49:55.260Z",
      |  "checksum": "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
      |  "size": 1024
      |}
      |""".stripMargin
  )

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |  "fileName": 123,
      |  "fileMimeType": true,
      |  "uploadTimestamp": 456,
      |  "checksum": false,
      |  "size": "invalid"
      |}
      |""".stripMargin
  )

  val uploadDetails: UploadDetails = UploadDetails(
    fileName = "test.pdf",
    fileMimeType = "application/pdf",
    uploadTimestamp = "2026-11-19T14:49:55.260Z",
    checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
    size = 1024
  )

  "UploadDetails" when {
    "read from valid Json" should {
      "produce the expected object" in {
        json.as[UploadDetails] shouldBe uploadDetails
      }
    }

    "read from invalid Json" should {
      "produce a JsError" in {
        invalidJson.validate[UploadDetails] shouldBe a[JsError]
      }
    }

    "written to Json" should {
      "produce the expected JsObject" in {
        Json.toJson(uploadDetails) shouldBe json
      }
    }
  }

}
