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

import api.connectors.DownstreamOutcome
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.retrieveSupplementaryInformation.RetrieveSupplementaryInformationFixtures.*
import v1.retrieveSupplementaryInformation.model.request.RetrieveSupplementaryInformationRequestData
import v1.retrieveSupplementaryInformation.model.response.RetrieveSupplementaryInformationResponse

import scala.concurrent.Future

class RetrieveSupplementaryInformationServiceSpec extends ServiceSpec with MockRetrieveSupplementaryInformationConnector {

  private val nino        = Nino("AA123456A")
  private val taxYear     = TaxYear.fromMtd("2026-27")
  private val requestData = RetrieveSupplementaryInformationRequestData(nino, taxYear)

  trait Test {
    val service = new RetrieveSupplementaryInformationService(mockRetrieveSupplementaryInformationConnector)
  }

  "RetrieveSupplementaryInformationService" when {
    "retrieve is called" should {
      "return a success response" in new Test {
        val outcome: DownstreamOutcome[RetrieveSupplementaryInformationResponse] = Right(ResponseWrapper(correlationId, responseModel))

        MockedRetrieveSupplementaryInformationConnector
          .retrieve(requestData)
          .returns(Future.successful(outcome))

        await(service.retrieve(requestData)) shouldBe outcome
      }

      "map errors according to the spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the connector" in new Test {
            MockedRetrieveSupplementaryInformationConnector
              .retrieve(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("1117", TaxYearFormatError),
          ("1215", NinoFormatError),
          ("1216", InternalError),
          ("1267", InternalError),
          ("5010", NotFoundError),
          ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError)
        )

        errors.foreach(serviceError.tupled)
      }
    }
  }

}
