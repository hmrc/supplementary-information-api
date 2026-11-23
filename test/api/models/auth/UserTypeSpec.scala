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

import api.models.auth.UserType.*
import api.utils.UnitSpec
import api.utils.enums.EnumJsonSpecSupport

class UserTypeSpec extends UnitSpec with EnumJsonSpecSupport {

  testSerialization[UserType](
    Individual         -> "Individual",
    Organisation       -> "Organisation",
    Agent              -> "Agent",
    `Supporting Agent` -> "Supporting Agent"
  )

}
