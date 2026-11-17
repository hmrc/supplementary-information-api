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

import api.config.AppConfig
import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.auth.UserType
import api.models.auth.UserType.{Agent, Individual, Organisation, SupportingAgent}
import api.utils.DateUtils.currentTimestamp
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v1.initiateSupplementaryDocumentUpload.model.request.{CreateSkeletalMetadataRecordRequest, InitiateSupplementaryDocumentUploadRequestData}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateSkeletalMetadataRecordConnector @Inject() (
    val http: HttpClientV2,
    val appConfig: AppConfig
) extends BaseDownstreamConnector {

  def createSkeletalMetadataRecord(request: InitiateSupplementaryDocumentUploadRequestData, fileId: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request.*

    val downstreamUri = HipUri[Unit](s"itsd/attachment-metadata/$nino/$fileId?taxYear=${taxYear.asTysDownstream}")

    val downstreamRequest = CreateSkeletalMetadataRecordRequest(
      uploadedBy = uploadedBy(userType),
      uploadTimestamp = currentTimestamp,
      status = "requested"
    )

    post(downstreamRequest, downstreamUri)
  }

  private def uploadedBy(userType: UserType): String = userType match {
    case Individual      => "individual"
    case Organisation    => "individual"
    case Agent           => "agent"
    case SupportingAgent => "agent"
  }

}
