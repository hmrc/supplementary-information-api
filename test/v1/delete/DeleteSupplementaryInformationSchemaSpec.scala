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

package v1.delete

import api.models.errors._
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}

class DeleteSupplementaryInformationSchemaSpec extends UnitSpec {

  "schema lookup" when {
    "a valid tax year is supplied" must {
      "use Def1 schema for 2026-27 and later" in {
        DeleteSupplementaryInformationSchema.schemaFor("2026-27") shouldBe Valid(DeleteSupplementaryInformationSchema.Def1)
        DeleteSupplementaryInformationSchema.schemaFor("2027-28") shouldBe Valid(DeleteSupplementaryInformationSchema.Def1)
      }
    }

    "an invalid tax year is supplied" must {
      "return RuleTaxYearNotSupportedError for years before 2026-27" in {
        DeleteSupplementaryInformationSchema.schemaFor("2025-26") shouldBe Invalid(Seq(RuleTaxYearNotSupportedError))
      }

      "return RuleTaxYearRangeInvalidError for invalid range" in {
        DeleteSupplementaryInformationSchema.schemaFor("2026-28") shouldBe Invalid(Seq(RuleTaxYearRangeInvalidError))
      }

      "return TaxYearFormatError for invalid format" in {
        DeleteSupplementaryInformationSchema.schemaFor("2026") shouldBe Invalid(Seq(TaxYearFormatError))
      }
    }
  }

}