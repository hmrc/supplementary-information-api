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
import api.utils.UnitSpec
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadFixtures.*
import v1.initiateSupplementaryDocumentUpload.model.response.InitiateSupplementaryDocumentUploadResponse

class UpscanHttpParserSpec extends UnitSpec {

  import UpscanHttpParser.*

  private val method                         = "POST"
  private val url                            = "upscan/v2/initiate"
  private implicit val correlationId: String = "test-correlation-id"

  private val httpReads: HttpReads[DownstreamOutcome[InitiateSupplementaryDocumentUploadResponse]] = implicitly
  private val expectedInternalError = ResponseWrapper(correlationId, OutboundError(InternalError))

  "UpscanHttpParser" should {
    "parse a successful OK JSON response" in {
      val response = HttpResponse(OK, upscanResponseJson.toString)

      httpReads.read(method, url, response) shouldBe Right(ResponseWrapper(correlationId, mtdResponseModel))
    }

    "return an internal error for any error response" when {
      "the response body is in JSON format" in {
        val errorResponse = Json.obj("statusCode" -> BAD_REQUEST, "message" -> "Invalid PrepareUploadRequest payload").toString
        val response      = HttpResponse(BAD_REQUEST, errorResponse)
        httpReads.read(method, url, response) shouldBe Left(expectedInternalError)
      }
      "the response body is in plain text format" in {
        val errorResponse = "Unavailable"
        val response      = HttpResponse(SERVICE_UNAVAILABLE, errorResponse)
        httpReads.read(method, url, response) shouldBe Left(expectedInternalError)
      }
    }
  }

}
