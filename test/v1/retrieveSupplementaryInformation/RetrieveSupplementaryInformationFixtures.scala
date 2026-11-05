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

package v1.retrieveSupplementaryInformation

import api.models.domain.Timestamp
import play.api.libs.json.{JsValue, Json}
import v1.retrieveSupplementaryInformation.model.response.RetrieveSupplementaryInformationResponse

object RetrieveSupplementaryInformationFixtures {

  // What HIP returns (note "whiteSpace" and no milliseconds)
  val downstreamResponseJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2026-08-24T14:15:22Z",
      |  "whiteSpace": "This is supplementary information 123 & - * ."
      |}
    """.stripMargin
  )

  // What MTD returns (note "supplementaryInformation" and milliseconds)
  val mtdResponseJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2026-08-24T14:15:22.000Z",
      |  "supplementaryInformation": "This is supplementary information 123 & - * ."
      |}
    """.stripMargin
  )

  val responseModel: RetrieveSupplementaryInformationResponse =
    RetrieveSupplementaryInformationResponse(
      submittedOn = Timestamp("2026-08-24T14:15:22.000Z"),
      supplementaryInformation = "This is supplementary information 123 & - * ."
    )

}
