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

import api.models.errors.DocumentIdFormatError
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}

class ResolveUuidSpec extends UnitSpec {

  private val validUuid = "fe4440a8-13dd-4f9e-961a-f8b8c5b3872d"

  "ResolveUuid" should {
    "return the value" when {
      "given a valid UUID" in {
        ResolveUuid(validUuid, DocumentIdFormatError) shouldBe Valid(validUuid)
      }

      "given a valid UUID in an Option" in {
        ResolveUuid(Some(validUuid), DocumentIdFormatError) shouldBe Valid(Some(validUuid))
      }

      "given an empty Option" in {
        ResolveUuid(None, DocumentIdFormatError) shouldBe Valid(None)
      }
    }

    "return the supplied error" when {
      "given a value that is not a UUID" in {
        ResolveUuid("not-a-uuid", DocumentIdFormatError) shouldBe Invalid(List(DocumentIdFormatError))
      }

      "given an uppercase UUID" in {
        ResolveUuid(Some(validUuid.toUpperCase), DocumentIdFormatError) shouldBe Invalid(List(DocumentIdFormatError))
      }
    }
  }

}
