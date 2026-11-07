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

package v1.retrieveSupplementaryInformation

import api.config.MockAppConfig
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.utils.UnitSpec
import v1.retrieveSupplementaryInformation.model.request.RetrieveSupplementaryInformationRequestData

class RetrieveSupplementaryInformationValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private trait Test {

    MockedAppConfig.minimumTaxYear.returns(2027).anyNumberOfTimes()

  }

  private implicit val correlationId: String = "someCorrelationId"

  private val validNino = "AA123456A"
  private val factory   = new RetrieveSupplementaryInformationValidatorFactory

  private def validate(nino: String = validNino, taxYear: String) =
    factory.validator(nino, taxYear).validateAndWrapResult()

  "RetrieveSupplementaryInformationValidatorFactory" should {
    "use the validator" when {
      "the minimum supported tax year is supplied" in new Test {
        validate(taxYear = "2026-27") shouldBe
          Right(RetrieveSupplementaryInformationRequestData(Nino(validNino), TaxYear.fromMtd("2026-27")))
      }
    }

    "return NinoFormatError" when {
      "the nino is invalid" in new Test {
        validate(nino = "invalid-nino", taxYear = "2026-27") shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "the tax year is before 2026-27" in new Test {
        validate(taxYear = "2025-26") shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return TaxYearFormatError" when {
      "the tax year format is invalid" in new Test {
        validate(taxYear = "20267") shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "the tax year range is invalid" in new Test {
        validate(taxYear = "2026-28") shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "the nino and the tax year format are both invalid" in new Test {
        validate(nino = "invalid-nino", taxYear = "invalid-tax-year") shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
