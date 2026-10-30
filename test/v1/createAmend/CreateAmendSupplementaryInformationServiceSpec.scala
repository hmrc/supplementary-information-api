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

package v1.createAmend

import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.createAmend.def1.fixture.Def1_CreateAmendSupplementaryInformationFixture.*
import v1.createAmend.def1.model.request.Def1_CreateAmendTaxLiabilityAdjustmentsRequestData

import scala.concurrent.Future

class CreateAmendSupplementaryInformationServiceSpec extends ServiceSpec {

  val requestData: Def1_CreateAmendSupplementaryInformationRequestData = Def1_CreateAmendSupplementaryInformationRequestData(
    Nino("AA123456A"),
    TaxYear.fromMtd("2026-27"),
    requestBodyModel
  )

  "CreateAmendTaxLiabilityAdjustmentsService" should {
    "return a success response" when {
      "using schema Def1" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
        MockCreateAmendTaxLiabilityAdjustmentsConnector
          .createAmendTaxLiabilityAdjustments(requestData)
          .returns(Future.successful(outcome))
        await(service.createAmendTaxLiabilityAdjustments(requestData)) shouldBe outcome
      }
    }

    "map errors according to spec" when {
      def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
        s"a $downStreamErrorCode error is returned from the service" in new Test {
          MockCreateAmendTaxLiabilityAdjustmentsConnector
            .createAmendTaxLiabilityAdjustments(requestData)
            .returns(
              Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode)))))
            )

          await(service.createAmendTaxLiabilityAdjustments(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errorMap = List(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_CORRELATION_ID"    -> InternalError,
        "INVALID_PAYLOAD"           -> InternalError,
        "INVALID_SUBMISSION"        -> RuleTaxYearNotEndedError,
        "TAX_YEAR_NOT_SUPPORTED"    -> InternalError,
        "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      errorMap.foreach(args => serviceError.tupled(args))
    }
  }

  trait Test extends MockCreateAmendTaxLiabilityAdjustmentsConnector {
    val service = new CreateAmendTaxLiabilityAdjustmentsService(mockConnector)
  }

}
