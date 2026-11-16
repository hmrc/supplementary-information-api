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
import api.connectors.DownstreamUri.UpscanUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v1.initiateSupplementaryDocumentUpload.model.request.{InitiateSupplementaryDocumentUploadRequestData, UpscanInitiateRequest}
import v1.initiateSupplementaryDocumentUpload.model.response.InitiateSupplementaryDocumentUploadResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpscanInitiateConnector @Inject() (
    val http: HttpClientV2,
    val appConfig: AppConfig
) extends BaseDownstreamConnector {

  def upscanInitiate(request: InitiateSupplementaryDocumentUploadRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[InitiateSupplementaryDocumentUploadResponse]] = {

    val downstreamUri = UpscanUri[InitiateSupplementaryDocumentUploadResponse]("upscan/v2/initiate")

    val upscanRequest = UpscanInitiateRequest(
      callbackUrl = appConfig.upscanCallbackUrl, // TODO update callback url when available
      successRedirect = request.body.flatMap(_.successRedirect),
      errorRedirect = request.body.flatMap(_.errorRedirect),
      maximumFileSize = appConfig.upscanMaxFileSize
    )

    post(upscanRequest, downstreamUri)
  }

}
