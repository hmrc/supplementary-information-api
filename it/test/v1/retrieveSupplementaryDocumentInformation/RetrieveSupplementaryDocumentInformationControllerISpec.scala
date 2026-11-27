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

package v1.retrieveSupplementaryDocumentInformation

import api.models.domain.TaxYear
import api.models.errors.*
import api.services.*
import api.support.IntegrationBaseSpec
import v1.retrieveSupplementaryDocumentInformation.model.response.RetrieveSupplementaryDocumentInformationResponse
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION

class RetrieveSupplementaryDocumentInformationControllerISpec extends IntegrationBaseSpec {

  "Calling the Retrieve Supplementary Document Information endpoint" should {
    "return a 200 status code" when {
      "a valid request is made and a success response body is returned" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()

          DownstreamStub.onSuccess(
            method = DownstreamStub.GET,
            uri = downstreamUri,
            queryParams = downstreamQueryParams,
            status = OK,
            body = RetrieveSupplementaryDocumentInformationFixtures.downstreamResponseJson
          )
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe RetrieveSupplementaryDocumentInformationFixtures.mtdResponseJson
      }
    }

    "return only the matching documents" when {
      "query parameters are supplied" in new Test {
        override def queryParams: Seq[(String, String)] = List("status" -> "requested", "status" -> "failed")

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()

          DownstreamStub.onSuccess(
            method = DownstreamStub.GET,
            uri = downstreamUri,
            queryParams = downstreamQueryParams,
            status = OK,
            body = RetrieveSupplementaryDocumentInformationFixtures.downstreamResponseJson
          )
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe Json.toJson(
          RetrieveSupplementaryDocumentInformationResponse(List(RetrieveSupplementaryDocumentInformationFixtures.requestedDocument)))
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1123A", "2026-27", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "invalid", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2025-27", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2025-26", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        input.foreach(validationErrorTest.tupled)
      }

      "query parameter validation error" when {
        def queryParamErrorTest(params: Seq[(String, String)], expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {
            override def queryParams: Seq[(String, String)] = params

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          (List("documentId" -> "not-a-uuid"), DocumentIdFormatError),
          (List("selected" -> "yes"), SelectedFormatError),
          (List("status" -> "success", "status" -> "banana"), StatusFormatError)
        )

        input.foreach(queryParamErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a $downstreamCode error and status $downstreamStatus" in new Test {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()

              DownstreamStub.onError(
                method = DownstreamStub.GET,
                uri = downstreamUri,
                queryParams = downstreamQueryParams,
                errorStatus = downstreamStatus,
                errorBody = errorBody(downstreamCode)
              )
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = Seq(
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (NOT_IMPLEMENTED, "5000", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {
    val nino: String    = "AA123456A"
    val taxYear: String = "2026-27"

    def queryParams: Seq[(String, String)] = Nil

    def setupStubs(): StubMapping

    private def mtdUri: String = s"/document/$nino/$taxYear"

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()

      buildRequest(mtdUri)
        .withQueryStringParameters(queryParams*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
        |[
        |  {
        |    "errorCode": "$code",
        |    "errorDescription": "downstream message"
        |  }
        |]
      """.stripMargin

    def downstreamUri: String = s"/itsd/attachment-metadata/$nino"

    def downstreamQueryParams: Map[String, String] = Map("taxYear" -> TaxYear.fromMtd(taxYear).asTysDownstream)
  }

}
