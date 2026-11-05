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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.retrieveSupplementaryInformation.RetrieveSupplementaryInformationFixtures.*
import v1.retrieveSupplementaryInformation.model.request.RetrieveSupplementaryInformationRequestData
import v1.retrieveSupplementaryInformation.model.response.RetrieveSupplementaryInformationResponse

import scala.concurrent.Future

class RetrieveSupplementaryInformationConnectorSpec extends ConnectorSpec {

  private type Outcome = DownstreamOutcome[RetrieveSupplementaryInformationResponse]

  private val nino        = Nino("AA123456A")
  private val taxYear     = TaxYear.fromMtd("2026-27")
  private val requestData = RetrieveSupplementaryInformationRequestData(nino, taxYear)

  "RetrieveSupplementaryInformationConnector" when {
    "retrieve is called" should {
      "return a success response" in new HipTest with Test {
        val outcome: DownstreamOutcome[RetrieveSupplementaryInformationResponse] = Right(ResponseWrapper(correlationId, responseModel))

        willGet[Outcome](url"$baseUrl/itsd/white-space/$nino", parameters = Seq("taxYear" -> "26-27"))
          .returns(Future.successful(outcome))

        await(connector.retrieve(requestData)) shouldBe outcome
      }

      "return an error response" in new HipTest with Test {
        val outcome: DownstreamOutcome[RetrieveSupplementaryInformationResponse] =
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("5010"))))

        willGet[Outcome](url"$baseUrl/itsd/white-space/$nino", parameters = Seq("taxYear" -> "26-27"))
          .returns(Future.successful(outcome))

        await(connector.retrieve(requestData)) shouldBe outcome
      }
    }
  }

  trait Test { self: ConnectorTest =>

    val connector: RetrieveSupplementaryInformationConnector =
      new RetrieveSupplementaryInformationConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

}
