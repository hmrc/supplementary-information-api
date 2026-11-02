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

package v1.deleteSupplementaryInformation

import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.utils.UnitSpec
import v1.deleteSupplementaryInformation.model.request.DeleteSupplementaryInformationRequestData

class DeleteSupplementaryInformationValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2026-27"

  private val validatorFactory = new DeleteSupplementaryInformationValidatorFactory

  "validator" should {
    "return the Def1 validator" when {
      "given a valid request" in {
        val result = validatorFactory.validator(validNino, validTaxYear)
        result shouldBe a[DeleteSupplementaryInformationValidator]
      }
    }

    "return a validation error" when {
      "given an invalid tax year" in {
        val result: Either[ErrorWrapper, DeleteSupplementaryInformationRequestData] =
          validatorFactory.validator(validNino, "not-a-tax-year").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "given an invalid nino" in {
        val result: Either[ErrorWrapper, DeleteSupplementaryInformationRequestData] =
          validatorFactory.validator("A12344A", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
  }
}
