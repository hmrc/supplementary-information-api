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

package v1.createAmend

import api.models.domain.TaxYear
import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import api.support.IntegrationBaseSpec
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import v1.createAmend.def1.fixture.Def1_CreateAmendSupplementaryInformationFixture.requestBodyJson

class CreateAmendSupplementaryInformationControllerISpec extends IntegrationBaseSpec {

  private val parsedTaxYear: TaxYear = TaxYear.fromMtd("2026-27")

  private val invalidSupplementaryInformationJson: JsValue = Json.parse(
    """
      |{
      |  "supplementaryInformation": "invalid !!!"
      |}
    """.stripMargin
  )

  "Calling the Create or Amend Supplementary Information endpoint" should {
    "return a 204 status code" when {
      "any valid request is made with a supported tax year" in new Test {
        override val taxYear: String = parsedTaxYear.asMtd

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(
            method = DownstreamStub.PUT,
            uri = downstreamUri,
            queryParams = queryParams,
            status = NO_CONTENT,
            body = JsObject.empty
          )
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {
            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
            }

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = List(
          ("AA1123A", "2026-27", requestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "invalid", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2025-27", requestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2025-26", requestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2026-27", invalidSupplementaryInformationJson, BAD_REQUEST, FormatSupplementaryInformationError),
          ("AA123456A", "2026-27", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError)
        )

        input.foreach(validationErrorTest.tupled)
      }
    }
    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {
          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            MtdIdLookupStub.ninoFound(nino)
            AuthStub.authorised()
            DownstreamStub.onError(
              method = DownstreamStub.PUT,
              uri = downstreamUri,
              queryParams = queryParams,
              errorStatus = downstreamStatus,
              errorBody = errorBody(downstreamCode)
            )
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.json shouldBe Json.toJson(expectedBody)
          response.status shouldBe expectedStatus
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val errors = List(
        (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
        (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError)
      )

      errors.foreach(serviceErrorTest.tupled)
    }
  }

  private trait Test {
    val nino: String    = "AA123456A"
    val taxYear: String = parsedTaxYear.asMtd

    def setupStubs(): StubMapping

    private def mtdUri: String = s"/$nino/$taxYear"

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()

      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def downstreamUri: String = s"/itsd/white-space/$nino"

    val queryParams: Map[String, String] = Map("taxYear" -> parsedTaxYear.asTysDownstream)

    def errorBody(code: String): String =
      s"""
         |{
         |  "origin": "HoD",
         |  "response": {
         |    "failures": [
         |      {
         |        "type": "$code",
         |        "reason": "downstream message"
         |      }
         |    ]
         |  }
         |}
      """.stripMargin

  }

}
