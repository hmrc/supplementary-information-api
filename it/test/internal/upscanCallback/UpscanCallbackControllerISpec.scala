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

import api.models.errors.{InternalError, MtdError, RuleIncorrectOrEmptyBodyError}
import api.services.DownstreamStub
import api.support.IntegrationBaseSpec
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import internal.upscanCallback.UpscanCallbackFixtures.successJson
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*

class UpscanCallbackControllerISpec extends IntegrationBaseSpec {

  "Calling the upscanCallback" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          DownstreamStub.onSuccess(
            method = DownstreamStub.PATCH,
            uri = downstreamUri,
            status = NO_CONTENT,
            body = JsObject.empty
          )
        }

        val response: WSResponse = await(request().post(successJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation error" when {
        "validation fails with RuleIncorrectOrEmptyBodyError error" in new Test {

          val response: WSResponse = await(request().post(JsObject.empty))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleIncorrectOrEmptyBodyError)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }
    }

    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {
          override def setupStubs(): StubMapping = {
            DownstreamStub.onError(
              method = DownstreamStub.PATCH,
              uri = downstreamUri,
              errorStatus = downstreamStatus,
              errorBody = errorBody(downstreamCode)
            )
          }

          val response: WSResponse = await(request().post(successJson))
          response.json shouldBe Json.toJson(expectedBody)
          response.status shouldBe expectedStatus
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val errors = List(
        (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "1289", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "5010", INTERNAL_SERVER_ERROR, InternalError)
      )

      errors.foreach(serviceErrorTest.tupled)
    }
  }

  private trait Test {
    private val reference: String = "11370e18-6e24-453e-b45a-76d3e32ea33d"

    def setupStubs(): StubMapping = StubMapping.NOT_CONFIGURED

    private def mtdUri: String = s"/upscan-callback"

    def request(): WSRequest = {
      setupStubs()

      buildRequest(mtdUri)
        .withHttpHeaders(
          (CONTENT_TYPE, "application/json")
        )
    }

    def downstreamUri: String = s"/itsd/attachment-metadata/$reference"

    def errorBody(code: String): String =
      s"""
         |[
         |  {
         |    "errorCode": "$code",
         |    "errorDescription": "downstream message"
         |  }
         |]
      """.stripMargin

  }

}
