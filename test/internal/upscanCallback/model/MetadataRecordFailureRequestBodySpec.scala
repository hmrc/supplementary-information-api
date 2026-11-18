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

class MetadataRecordFailureRequestBodySpec extends UnitSpec {

  val json: JsValue = Json.parse(
    """
      |{
      |  "uploadTimestamp": "2026-11-18T00:00:00Z",
      |  "status": "failed",
      |  "statusReason": "This file has a virus"
      |}
      |""".stripMargin
  )

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |  "uploadTimestamp": 123,
      |  "status": true,
      |  "statusReason": 456
      |}
      |""".stripMargin
  )

  val metadataRecordFailureRequestBody: MetadataRecordFailureRequestBody =
    MetadataRecordFailureRequestBody(
      uploadTimestamp = "2026-11-18T00:00:00Z",
      status = "failed",
      statusReason = "This file has a virus"
    )

  "MetadataRecordFailureRequestBody" when {
    "read from valid Json" should {
      "produce the expected object" in {
        json.as[MetadataRecordFailureRequestBody] shouldBe metadataRecordFailureRequestBody
      }
    }

    "read from invalid Json" should {
      "produce a JsError" in {
        invalidJson.validate[MetadataRecordFailureRequestBody] shouldBe a[JsError]
      }
    }

    "written to Json" should {
      "produce the expected JsObject" in {
        Json.toJson(metadataRecordFailureRequestBody) shouldBe json
      }
    }
  }

}
