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

import api.controllers.RequestContext
import api.models.errors.{MtdError, *}
import api.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import cats.implicits.*
import v1.initiateSupplementaryDocumentUpload.model.request.InitiateSupplementaryDocumentUploadRequestData
import v1.initiateSupplementaryDocumentUpload.model.response.InitiateSupplementaryDocumentUploadResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InitiateSupplementaryDocumentUploadService @Inject() (upscanConnector: UpscanInitiateConnector,
                                                            metadataConnector: CreateSkeletalMetadataRecordConnector)
    extends BaseService {

  def initiateSupplementaryDocumentUpload(request: InitiateSupplementaryDocumentUploadRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[InitiateSupplementaryDocumentUploadResponse]] = {

    val response = for {
      upscanResponse <- EitherT(upscanConnector.upscanInitiate(request)).leftMap(mapDownstreamErrors(upscanErrorMap))
      fileId = upscanResponse.responseData.documentId
      metadataResponse <- EitherT(metadataConnector.createSkeletalMetadataRecord(request, fileId)).leftMap(mapDownstreamErrors(metadataErrorMap))
    } yield upscanResponse.copy(correlationId = metadataResponse.correlationId)

    response.value
  }

  private val upscanErrorMap: Map[String, MtdError] = Map.empty

  private val metadataErrorMap: Map[String, MtdError] = Map(
    "1215" -> NinoFormatError,
    "1117" -> TaxYearFormatError,
    "1289" -> InternalError,
    "1216" -> InternalError,
    "1000" -> InternalError
  )

}
