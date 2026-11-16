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

package v1.initiateSupplementaryDocumentUpload

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadFixtures.*
import v1.initiateSupplementaryDocumentUpload.model.response.InitiateSupplementaryDocumentUploadResponse

import scala.concurrent.Future

class UpscanInitiateConnectorSpec extends ConnectorSpec {

  private val downstreamUrl            = url"$baseUrl/upscan/v2/initiate"
  private val upscallCallbackUrl       = "https://callback"
  private val upscallMaxFileSize: Long = 5242880

  trait Test extends ConnectorTest {

    val requiredHeaders: Seq[(String, String)] = Seq(
      "CorrelationId" -> correlationId,
      "User-Agent"    -> "supplementary-information-api"
    )

    val connector = new UpscanInitiateConnector(mockHttpClient, mockAppConfig)

    MockedAppConfig.upscanInitiateBaseUrl.returns(baseUrl)
    MockedAppConfig.upscanCallbackUrl.returns(upscallCallbackUrl)
    MockedAppConfig.upscanMaxFileSize.returns(upscallMaxFileSize)
  }

  "UpscanInitiateConnector" should {
    "post the upload request with the required headers and return the response" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, responseModel))

      willPost(downstreamUrl, upscanRequestModel).returns(Future.successful(outcome))

      await(connector.upscanInitiate(requestDataModel)) shouldBe outcome
    }

    "omit redirect URLs when no request body is supplied" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, responseModel))
      val body    = upscanRequestModel.copy(successRedirect = None, errorRedirect = None)

      willPost(downstreamUrl, body).returns(Future.successful(outcome))

      await(connector.upscanInitiate(requestDataModel.copy(body = None))) shouldBe outcome
    }

    "return downstream errors unchanged" in new Test {
      val outcome: DownstreamOutcome[InitiateSupplementaryDocumentUploadResponse] =
        Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("UNKNOWN_ERROR"))))

      willPost(downstreamUrl, upscanRequestModel).returns(Future.successful(outcome))

      await(connector.upscanInitiate(requestDataModel)) shouldBe outcome
    }
  }

}
