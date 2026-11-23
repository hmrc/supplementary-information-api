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
import api.models.auth.UserType
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.initiateSupplementaryDocumentUpload.model.request.InitiateSupplementaryDocumentUploadRequestData
import v1.initiateSupplementaryDocumentUpload.model.response.{InitiateSupplementaryDocumentUploadResponse, UploadRequest}

import scala.concurrent.Future

class InitiateSupplementaryDocumentUploadServiceSpec
    extends ServiceSpec
    with MockUpscanInitiateConnector
    with MockCreateSkeletalMetadataRecordConnector {

  private val fileId = "6272100a-5672-402f-9ab3-abfcb6058780"

  private val requestData =
    InitiateSupplementaryDocumentUploadRequestData(Nino("AA123456A"), TaxYear.fromMtd("2026-27"), UserType.Individual, None)

  private val response = InitiateSupplementaryDocumentUploadResponse(
    documentId = fileId,
    uploadRequest = UploadRequest("https://example.com", Map("key" -> "value"))
  )

  private val service = new InitiateSupplementaryDocumentUploadService(mockUpscanInitiateConnector, mockCreateSkeletalMetadataRecordConnector)

  private val upscanOutcome: DownstreamOutcome[InitiateSupplementaryDocumentUploadResponse] =
    Right(ResponseWrapper("", response))

  private val metadataOutcome: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

  private val mtdOutcome: DownstreamOutcome[InitiateSupplementaryDocumentUploadResponse] =
    Right(ResponseWrapper(correlationId, response))

  "InitiateSupplementaryDocumentUploadService" when {
    "initiateSupplementaryDocumentUpload is called" should {
      "return a success response with expected body" in {
        MockedUpscanInitiateConnector
          .upscanInitiate(requestData)
          .returns(Future.successful(upscanOutcome))

        MockedCreateSkeletalMetadataRecordConnector.createSkeletalMetadataRecord(requestData, fileId).returns(Future.successful(metadataOutcome))

        await(service.initiateSupplementaryDocumentUpload(requestData)) shouldBe mtdOutcome
      }

      "map upscan downstream errors to an internal error" in {
        MockedUpscanInitiateConnector
          .upscanInitiate(requestData)
          .returns(
            Future.successful(
              Left(ResponseWrapper("", DownstreamErrors.single(DownstreamErrorCode("UNKNOWN_ERROR"))))
            ))

        await(service.initiateSupplementaryDocumentUpload(requestData)) shouldBe Left(ErrorWrapper("", InternalError))
      }

      "map metadata errors according to spec" when {
        def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
          s"a $downStreamErrorCode error is returned from the service" in {
            MockedUpscanInitiateConnector
              .upscanInitiate(requestData)
              .returns(Future.successful(upscanOutcome))

            MockedCreateSkeletalMetadataRecordConnector
              .createSkeletalMetadataRecord(requestData, fileId)
              .returns(
                Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode)))))
              )

            await(service.initiateSupplementaryDocumentUpload(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errorMap = List(
          "1215" -> NinoFormatError,
          "1117" -> TaxYearFormatError,
          "1289" -> InternalError,
          "1216" -> InternalError,
          "1000" -> InternalError
        )

        errorMap.foreach(args => serviceError.tupled(args))
      }
    }
  }

}
