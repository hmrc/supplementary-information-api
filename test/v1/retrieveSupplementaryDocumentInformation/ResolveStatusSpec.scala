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

package v1.retrieveSupplementaryDocumentInformation

import api.models.errors.StatusFormatError
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}
import v1.retrieveSupplementaryDocumentInformation.model.response.DocumentStatus

class ResolveStatusSpec extends UnitSpec {

  "ResolveStatus" should {
    "return no statuses" when {
      "none are supplied" in {
        ResolveStatus(Nil) shouldBe Valid(Nil)
      }
    }

    "return the parsed statuses" when {
      "valid statuses are supplied" in {
        ResolveStatus(List("requested", "success", "failed")) shouldBe
          Valid(List(DocumentStatus.requested, DocumentStatus.success, DocumentStatus.failed))
      }
    }

    "return a single StatusFormatError" when {
      "one status is invalid" in {
        ResolveStatus(List("success", "banana")) shouldBe Invalid(List(StatusFormatError))
      }

      "several statuses are invalid" in {
        ResolveStatus(List("banana", "apple")) shouldBe Invalid(List(StatusFormatError))
      }
    }
  }

}
