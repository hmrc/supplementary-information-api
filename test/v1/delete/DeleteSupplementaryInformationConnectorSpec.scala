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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.delete.def1.model.request.Def1_DeleteSupplementaryInformationRequestData
import v1.delete.model.request.DeleteSupplementaryInformationRequestData

import scala.concurrent.Future

class DeleteSupplementaryInformationConnectorSpec extends ConnectorSpec {

  private val nino: String    = "AA123456A"
  private val taxYear: String = "2026-27"

  lazy val request: DeleteSupplementaryInformationRequestData =
    Def1_DeleteSupplementaryInformationRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  "deleteSupplementaryInformation" must {
    "return a success response" in new HipTest with Test {

      val expected: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

      willDelete(url = url"$baseUrl/itsd/white-space/$nino?taxYear=26-27")
        .returning(Future.successful(expected))

      val result: DownstreamOutcome[Unit] = await(connector.deleteSupplementaryInformation(request))
      result shouldBe expected
    }
  }

  trait Test {
    self: ConnectorTest =>

    val connector: DeleteSupplementaryInformationConnector =
      new DeleteSupplementaryInformationConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

}
