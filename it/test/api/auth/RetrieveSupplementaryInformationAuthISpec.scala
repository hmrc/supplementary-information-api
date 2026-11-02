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

package api.auth

import api.services.DownstreamStub
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.OK

class RetrieveSupplementaryInformationAuthISpec extends AuthMainAgentsOnlyISpec {

  override protected val callingApiVersion: String = "1.0"

  override protected val supportingAgentsNotAllowedEndpoint: String = "retrieve-supplementary-information"

  override protected val mtdUrl: String = s"/$nino/2026-27"

  override protected val downstreamUri: String = s"/itsd/white-space/$nino"

  override protected val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  override protected val downstreamQueryParam: Map[String, String] = Map("taxYear" -> "26-27")

  override protected val downstreamSuccessStatus: Int = OK

  override protected val expectedMtdSuccessStatus: Int = OK

  override protected val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse("""
        |{
        |  "submittedOn": "2026-08-24T14:15:22Z",
        |  "whiteSpace": "Some supplementary information"
        |}
      """.stripMargin)
  )

  override protected def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

}
