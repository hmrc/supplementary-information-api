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

import api.config.MockAppConfig
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.utils.UnitSpec
import v1.retrieveSupplementaryDocumentInformation.model.request.RetrieveSupplementaryDocumentInformationRequestData
import v1.retrieveSupplementaryDocumentInformation.model.response.DocumentStatus

class RetrieveSupplementaryDocumentInformationValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private trait Test {

    MockedAppConfig.minimumTaxYear.returns(2027).anyNumberOfTimes()

  }

  private implicit val correlationId: String = "someCorrelationId"

  private val validNino       = "AA123456A"
  private val validTaxYear    = "2026-27"
  private val validDocumentId = "fe4440a8-13dd-4f9e-961a-f8b8c5b3872d"
  private val factory         = new RetrieveSupplementaryDocumentInformationValidatorFactory

  private def validate(
      nino: String = validNino,
      taxYear: String = validTaxYear,
      documentId: Option[String] = None,
      selected: Option[String] = None,
      status: Seq[String] = Nil
  ) =
    factory.validator(nino, taxYear, documentId, selected, status).validateAndWrapResult()

  private def parsed(documentId: Option[String] = None, selected: Option[Boolean] = None, statuses: Seq[DocumentStatus] = Nil) =
    Right(RetrieveSupplementaryDocumentInformationRequestData(Nino(validNino), TaxYear.fromMtd(validTaxYear), documentId, selected, statuses))

  "RetrieveSupplementaryDocumentInformationValidatorFactory" when {
    "validator is called" should {
      "return the parsed domain object" when {
        "no query parameters are supplied" in new Test {
          validate() shouldBe parsed()
        }

        "a valid documentId is supplied" in new Test {
          validate(documentId = Some(validDocumentId)) shouldBe parsed(documentId = Some(validDocumentId))
        }

        "selected=true is supplied" in new Test {
          validate(selected = Some("true")) shouldBe parsed(selected = Some(true))
        }

        "selected=false is supplied" in new Test {
          validate(selected = Some("false")) shouldBe parsed(selected = Some(false))
        }

        "a single status is supplied" in new Test {
          validate(status = List("failed")) shouldBe parsed(statuses = List(DocumentStatus.failed))
        }

        "multiple statuses are supplied" in new Test {
          validate(status = List("requested", "success")) shouldBe
            parsed(statuses = List(DocumentStatus.requested, DocumentStatus.success))
        }

        "all query parameters are supplied" in new Test {
          validate(documentId = Some(validDocumentId), selected = Some("true"), status = List("success")) shouldBe
            parsed(documentId = Some(validDocumentId), selected = Some(true), statuses = List(DocumentStatus.success))
        }
      }

      "return NinoFormatError" when {
        "the nino is invalid" in new Test {
          validate(nino = "invalid-nino") shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
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

      "return DocumentIdFormatError" when {
        "the documentId is not a UUID" in new Test {
          validate(documentId = Some("not-a-uuid")) shouldBe Left(ErrorWrapper(correlationId, DocumentIdFormatError))
        }
      }

      "return SelectedFormatError" when {
        "selected is not a boolean" in new Test {
          validate(selected = Some("yes")) shouldBe Left(ErrorWrapper(correlationId, SelectedFormatError))
        }
      }

      "return StatusFormatError" when {
        "the status is not one of requested, success or failed" in new Test {
          validate(status = List("failedAcknowledged")) shouldBe Left(ErrorWrapper(correlationId, StatusFormatError))
        }

        "one of several statuses is invalid" in new Test {
          validate(status = List("success", "banana")) shouldBe Left(ErrorWrapper(correlationId, StatusFormatError))
        }

        "several statuses are invalid (only returned once)" in new Test {
          validate(status = List("banana", "apple")) shouldBe Left(ErrorWrapper(correlationId, StatusFormatError))
        }
      }

      "return multiple errors" when {
        "the nino and the tax year format are both invalid" in new Test {
          validate(nino = "invalid-nino", taxYear = "invalid-tax-year") shouldBe
            Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
        }

        "all of the query parameters are invalid" in new Test {
          validate(documentId = Some("not-a-uuid"), selected = Some("yes"), status = List("banana")) shouldBe
            Left(ErrorWrapper(correlationId, BadRequestError, Some(List(DocumentIdFormatError, SelectedFormatError, StatusFormatError))))
        }
      }
    }
  }

}
