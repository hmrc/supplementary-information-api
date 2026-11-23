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

package api.connectors.httpparsers

import api.connectors.DownstreamOutcome
import api.models.errors.{InternalError, OutboundError}
import api.models.outcomes.ResponseWrapper
import play.api.http.Status.OK
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object UpscanHttpParser extends HttpParser {

  implicit def reads[A: Reads](implicit correlationId: String): HttpReads[DownstreamOutcome[A]] =
    (_: String, url: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          logger.info(
            "[UpscanHttpParser][read] - " +
              s"Success response received from downstream for request with correlationId: $correlationId when calling $url")

          response.validateJsonWithLogging[A] match {
            case Some(result) => Right(ResponseWrapper(correlationId, result))
            case None         => Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
          }

        case status =>
          logger.warn(
            "[UpscanHttpParser][read] - " +
              s"Error response received from downstream with status: $status and body\n" +
              s"${response.body} for request with correlationId: $correlationId when calling $url")

          Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
      }
    }

}
