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

import play.api.libs.json.JsObject
import api.models.errors.*
import api.services.{AuthStub, DownstreamStub, MtdIdLookupStub}
import api.support.IntegrationBaseSpec
import com.github.tomakehurst.wiremock.stubbing.StubMapping
//import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
//import play.api.libs.ws.DefaultBodyReadables.readableAsString
import InitiateSupplementaryDocumentUploadFixtures.*

class InitiateSupplementaryDocumentUploadControllerISpec extends IntegrationBaseSpec {

  "Calling the Initiate Supplementary Document Upload endpoint" should {
    "return a 200 status code and correct response body" when {
      "a valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()

          DownstreamStub.onSuccess(
            method = DownstreamStub.POST,
            uri = downstreamUri,
            status = OK,
            body = downstreamResponseJson
          )

        }

        val response: WSResponse = await(request().post(mtdRequestBodyJson))
        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
      }
    }

    "return an error according to the spec" when {
      "a validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
            }

            val response: WSResponse = await(request().post(mtdRequestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1123A", "2026-27", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "26-27", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2025-27", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2025-26", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        input.foreach(validationErrorTest.tupled)

        Seq(
          Seq("successRedirect"),
          Seq("errorRedirect"),
          Seq("successRedirect", "errorRedirect")
        ).foreach { fields =>
          s"body validation fails with FORMAT_REDIRECT_URL for invalid ${fields.mkString(" and ")}" in new Test {
            override def setupStubs(): StubMapping = {
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
            }

            val invalidBody: JsObject = mtdRequestBodyJson ++ JsObject(fields.map(_ -> Json.toJson("http://example.com")))
            val response: WSResponse  = await(request().post(invalidBody))

            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(RedirectUrlFormatError.withPaths(fields.map(field => s"/$field")))
          }
        }
      }

      "a downstream upscan error" which {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"returns $INTERNAL_SERVER_ERROR when upscan returns $downstreamCode and status $downstreamStatus" in new Test {
            override def setupStubs(): StubMapping = {
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()

              DownstreamStub.onError(
                method = DownstreamStub.POST,
                uri = downstreamUri,
                errorStatus = downstreamStatus,
                errorBody = errorBody(downstreamCode)
              )
            }

            val response: WSResponse = await(request().post(mtdRequestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }
        val input = List(
          (BAD_REQUEST, "Invalid request body", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "Upscan is unavailable", INTERNAL_SERVER_ERROR, InternalError),
          (OK, "{}", INTERNAL_SERVER_ERROR, InternalError)
        )

        input.foreach(args => serviceErrorTest.tupled(args))

      }

    }
  }

  private trait Test {
    val nino: String    = "AA123456A"
    val taxYear: String = "2026-27"

    def setupStubs(): StubMapping

    private def mtdUri: String = s"/document/$nino/$taxYear"

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()

      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        ) // TODO check headers
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

    def downstreamUri: String = "/upscan/v2/initiate"

  }

}
