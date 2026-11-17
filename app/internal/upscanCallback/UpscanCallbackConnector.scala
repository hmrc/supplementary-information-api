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

import api.config.AppConfig
import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import api.utils.DateUtils.nowAsUtc
import internal.upscanCallback.model.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpscanCallbackConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def handleCallback(request: UpscanCallbackRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val downstreamUri: DownstreamUri[Unit] = HipUri(s"itsd/attachment-metadata/${request.body.reference}")

    request.body match {
      case successBody: UpscanCallbackSuccessRequestBody => patch(body = toSuccessMetadataRecordBody(successBody), uri = downstreamUri)
      case failureBody: UpscanCallbackFailureRequestBody => patch(body = toFailureMetadataRecordBody(failureBody), uri = downstreamUri)
    }
  }

  private def toSuccessMetadataRecordBody(callbackBody: UpscanCallbackSuccessRequestBody): MetadataRecordSuccessRequestBody =
    MetadataRecordSuccessRequestBody(
      scanId = callbackBody.downloadUrl,
      uploadTimestamp = callbackBody.uploadDetails.uploadTimestamp,
      fileName = callbackBody.uploadDetails.fileName,
      fileSize = callbackBody.uploadDetails.size,
      status = callbackBody.fileStatus
    )

  private def toFailureMetadataRecordBody(callbackBody: UpscanCallbackFailureRequestBody): MetadataRecordFailureRequestBody =
    MetadataRecordFailureRequestBody(
      uploadTimestamp = nowAsUtc,
      status = callbackBody.fileStatus,
      statusReason = callbackBody.failureDetails.failureReason
    )

}
