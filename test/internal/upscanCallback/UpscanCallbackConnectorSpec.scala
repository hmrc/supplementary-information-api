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

package internal.upscanCallback

import api.connectors.ConnectorSpec
import api.models.outcomes.ResponseWrapper
import api.utils.DateUtils.nowAsUtc
import internal.upscanCallback.UpscanCallbackFixtures.{failureRequestBody, successRequestBody}
import internal.upscanCallback.model.*
import uk.gov.hmrc.http.StringContextOps

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future

class UpscanCallbackConnectorSpec extends ConnectorSpec {

  private val reference     = "11370e18-6e24-453e-b45a-76d3e32ea33d"
  private val downstreamUrl = url"$baseUrl/itsd/attachment-metadata/$reference"

  trait Test extends ConnectorTest {

    val connector: UpscanCallbackConnector = new UpscanCallbackConnector(http = mockHttpClient, appConfig = mockAppConfig)
  }

  "UpscanCallbackConnector" should {
    "handle successful callback" when {
      "file status is READY" in new HipTest with Test {

        val requestData = UpscanCallbackRequestData(body = successRequestBody)

        val expectedBody = MetadataRecordSuccessRequestBody(
          scanId = "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
          uploadTimestamp = "2026-11-19T14:49:55.260Z",
          fileName = "test.pdf",
          fileSize = 1024,
          status = "requested"
        )

        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
        willPatch(url = downstreamUrl, body = expectedBody).returns(Future.successful(outcome))
        await(connector.handleCallback(requestData)) shouldBe outcome
      }
    }

    "handle failure callback" when {
      "file status is FAILED" in new HipTest with Test {

        private implicit val clock: Clock = Clock.fixed(Instant.now, ZoneOffset.UTC)

        val requestData = UpscanCallbackRequestData(body = failureRequestBody)

        val expectedBody = MetadataRecordFailureRequestBody(
          uploadTimestamp = nowAsUtc(clock),
          status = "failed",
          statusReason = "QUARANTINE"
        )

        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
        willPatch(downstreamUrl, expectedBody).returns(Future.successful(outcome))
        await(connector.handleCallback(requestData)) shouldBe outcome
      }
    }
  }

}
