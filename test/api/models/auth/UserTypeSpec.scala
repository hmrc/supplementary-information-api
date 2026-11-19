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

package api.models.auth

import api.utils.UnitSpec
import play.api.libs.json.{JsString, Json}

class UserTypeSpec extends UnitSpec {

  "UserType" when {
    "Individual written to JSON" should {
      "produce the expected JsString(\"Individual\")" in {
        Json.toJson(UserType.Individual) shouldBe JsString("Individual")
      }
    }

    "Organisation written to JSON" should {
      "produce the expected JsString(\"Organisation\")" in {
        Json.toJson(UserType.Organisation) shouldBe JsString("Organisation")
      }
    }

    "Agent written to JSON" should {
      "produce the expected JsString(\"Agent\")" in {
        Json.toJson(UserType.Agent) shouldBe JsString("Agent")
      }
    }

    "SupportingAgent written to JSON" should {
      "produce the expected JsString(\"Supporting Agent\")" in {
        Json.toJson(UserType.SupportingAgent) shouldBe JsString("Supporting Agent")
      }
    }
  }

}
