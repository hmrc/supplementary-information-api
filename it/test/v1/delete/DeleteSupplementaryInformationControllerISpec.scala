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

package v1.delete

import api.models.errors._
import api.services._
import api.support.IntegrationBaseSpec
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION

class DeleteSupplementaryInformationControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    def taxYear: String
    def downstreamUri: String = s"/itsd/white-space/$nino"
    def downstreamQueryParams: Map[String, String] = Map("taxYear" -> "26-27")

    def mtdUri: String = s"/$nino/$taxYear"

    def setupStubs(): Unit = {}

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "downstream message"
         |}
         |""".stripMargin
  }

  "Calling the Delete Supplementary Information endpoint" should {
    "return a 204 status code" when {
      "a valid request is made" in new Test {
        override val taxYear: String = "2026-27"

        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(
            DownstreamStub.DELETE,
            downstreamUri,
            NO_CONTENT
          )
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.header("Content-Type") shouldBe None
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {
            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2026-27", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "2026", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2026-28", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2025-26", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        input.foreach(args => validationErrorTest.tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {
            override val taxYear: String = "2026-27"

            override def setupStubs(): Unit = {
              DownstreamStub.onError(
                DownstreamStub.DELETE,
                downstreamUri,
                downstreamQueryParams,
                downstreamStatus,
                errorBody(downstreamCode)
              )
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", INTERNAL_SERVER_ERROR, InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => serviceErrorTest.tupled(args))
      }
    }
  }
}