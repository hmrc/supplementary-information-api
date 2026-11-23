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
import api.models.auth.UserType.{Agent, Individual, Organisation, `Supporting Agent`}
import api.utils.DateUtils.nowAsUtc
import play.api.http.Status
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v1.initiateSupplementaryDocumentUpload.model.request.{CreateSkeletalMetadataRecordRequest, InitiateSupplementaryDocumentUploadRequestData}

import java.time.Clock
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
      correlationId: String,
      clock: Clock = Clock.systemUTC()): Future[DownstreamOutcome[Unit]] = {

    import request.*

    implicit val successCode: SuccessCode = SuccessCode(Status.CREATED)

    val downstreamUri = HipUri[Unit](s"itsd/attachment-metadata/$nino/$fileId?taxYear=${taxYear.asTysDownstream}")

    val downstreamRequest = CreateSkeletalMetadataRecordRequest(
      uploadedBy = uploadedBy(userType),
      uploadTimestamp = nowAsUtc,
      status = "requested"
    )

    post(downstreamRequest, downstreamUri)
  }

  private def uploadedBy(userType: UserType): String = userType match {
    case Individual | Organisation  => "individual"
    case Agent | `Supporting Agent` => "agent"
  }

}
