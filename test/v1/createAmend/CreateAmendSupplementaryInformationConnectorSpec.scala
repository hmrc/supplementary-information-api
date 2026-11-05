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

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import CreateAmendSupplementaryInformationFixtures.requestBodyModel
import v1.createAmend.model.request.CreateAmendSupplementaryInformationRequestData

import scala.concurrent.Future

class CreateAmendSupplementaryInformationConnectorSpec extends ConnectorSpec {

  private val nino          = "AA123456A"
  private val taxYear       = TaxYear.fromMtd("2026-27")
  private val downstreamUrl = url"$baseUrl/itsd/white-space/$nino?taxYear=26-27"

  trait Test extends ConnectorTest {

    val connector: CreateAmendSupplementaryInformationConnector = new CreateAmendSupplementaryInformationConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val requestData: CreateAmendSupplementaryInformationRequestData =
      CreateAmendSupplementaryInformationRequestData(Nino(nino), taxYear, requestBodyModel)

  }

  "CreateAmendSupplementaryInformationConnector" should {
    "return a valid response" when {
      "a valid request is made" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
        willPut(url = downstreamUrl, body = requestBodyModel).returns(Future.successful(outcome))
        await(connector.createAmendSupplementaryInformation(requestData)) shouldBe outcome
      }
    }
  }

}
