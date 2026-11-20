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

package api.models.request

import api.models.auth.UserDetails
import api.models.auth.UserType.Agent
import api.utils.UnitSpec
import play.api.test.FakeRequest

class ApiRequestSpec extends UnitSpec {

  private val mtdId = "X123567890"

  "UserRequest" should {
    "return user details when userDetailsOpt is called" in {
      val userDetails = UserDetails(mtdId, Agent, Some("arn"))

      val request     = FakeRequest()
      val userRequest = UserRequest(userDetails, request)

      userRequest.userDetailsOpt shouldBe Some(userDetails)
    }
  }

  "UnauthenticatedRequest" should {
    "return None when userDetailsOpt is called" in {
      val request                = FakeRequest()
      val unauthenticatedRequest = UnauthenticatedRequest(request)

      unauthenticatedRequest.userDetailsOpt shouldBe None
    }
  }

}
