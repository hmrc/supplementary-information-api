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

import api.connectors.DownstreamOutcome
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.initiateSupplementaryDocumentUpload.model.request.InitiateSupplementaryDocumentUploadRequestData
import v1.initiateSupplementaryDocumentUpload.model.response.{InitiateSupplementaryDocumentUploadResponse, UploadRequest}

import scala.concurrent.Future

class InitiateSupplementaryDocumentUploadServiceSpec extends ServiceSpec with MockUpscanInitiateConnector {

  private val requestData =
    InitiateSupplementaryDocumentUploadRequestData(Nino("AA123456A"), TaxYear.fromMtd("2026-27"), None)

  private val response = InitiateSupplementaryDocumentUploadResponse(
    documentId = "document-id",
    uploadRequest = UploadRequest("https://example.com", Map("key" -> "value"))
  )

  trait Test {
    val service = new InitiateSupplementaryDocumentUploadService(mockUpscanInitiateConnector)
  }

  "InitiateSupplementaryDocumentUploadService" when {
    "initiateSupplementaryDocumentUpload is called" should {
      "return a created response" in new Test {
        val outcome: DownstreamOutcome[InitiateSupplementaryDocumentUploadResponse] =
          Right(ResponseWrapper(correlationId, response))

        MockedUpscanInitiateConnector
          .upscanInitiate(requestData)
          .returns(Future.successful(outcome))

        await(service.initiateSupplementaryDocumentUpload(requestData)) shouldBe outcome
      }

      "map upscan downstream errors to an internal error" in new Test {
        MockedUpscanInitiateConnector
          .upscanInitiate(requestData)
          .returns(
            Future.successful(
              Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("UNKNOWN_ERROR"))))
            ))

        await(service.initiateSupplementaryDocumentUpload(requestData)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
      }

//      "map an unmatched stub error to a government test scenario error" in new Test {
//        MockedUpscanInitiateConnector
//          .upscanInitiate(requestData)
//          .returns(
//            Future.successful(
//              Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("UNMATCHED_STUB_ERROR"))))
//            ))
//
//        await(service.initiateSupplementaryDocumentUpload(requestData)) shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectGovTestScenarioError))
//      }
    }
  }

}
