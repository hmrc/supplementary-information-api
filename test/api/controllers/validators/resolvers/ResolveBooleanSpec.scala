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

package api.controllers.validators.resolvers

import api.models.errors.SelectedFormatError
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}

class ResolveBooleanSpec extends UnitSpec {

  "ResolveBoolean" should {
    "return the parsed boolean" when {
      "given true" in {
        ResolveBoolean("true", SelectedFormatError) shouldBe Valid(true)
      }

      "given false in an Option" in {
        ResolveBoolean(Some("false"), SelectedFormatError) shouldBe Valid(Some(false))
      }

      "given an empty Option" in {
        ResolveBoolean(None, SelectedFormatError) shouldBe Valid(None)
      }
    }

    "return the supplied error" when {
      "given a value that is not a boolean" in {
        ResolveBoolean("yes", SelectedFormatError) shouldBe Invalid(List(SelectedFormatError))
      }

      "given an Option with a value that is not a boolean" in {
        ResolveBoolean(Some("1"), SelectedFormatError) shouldBe Invalid(List(SelectedFormatError))
      }
    }
  }

}
