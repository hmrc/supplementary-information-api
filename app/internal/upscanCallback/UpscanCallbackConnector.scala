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
import api.utils.DateUtils.currentTimestamp
import internal.upscanCallback.model.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpscanCallbackConnector @Inject(val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def handleCallback(
                      request: UpscanCallbackRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val downstreamUri: DownstreamUri[Unit] = HipUri(s"itsd/attachment-metadata/${request.body.reference}")

    request.body match {
      case successBody: UpscanCallbackSuccessRequestBody => handleSuccessCallback(downstreamUri, successBody)
      case failureBody: UpscanCallbackFailureRequestBody => handleFailureCallback(downstreamUri, failureBody)
    }
  }

  private def handleSuccessCallback(downstreamUri: DownstreamUri[Unit], callbackBody: UpscanCallbackSuccessRequestBody)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {
    import callbackBody.*

    val body = MetadataRecordSuccessRequestBody(scanId = downloadUrl, uploadTimestamp = uploadDetails.uploadTimestamp, fileName = uploadDetails.fileName, fileSize = uploadDetails.size, status = fileStatus)

    patch(body = body, uri = downstreamUri)
  }

  private def handleFailureCallback(downstreamUri: DownstreamUri[Unit], callbackBody: UpscanCallbackFailureRequestBody)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {
    import callbackBody.*

    val body = MetadataRecordFailureRequestBody(uploadTimestamp = currentTimestamp, status = fileStatus, statusReason = failureDetails.failureReason)

    patch(body = body, uri = downstreamUri)
  }
}
