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

import api.connectors.ConnectorSpec
import api.models.auth.UserType
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadFixtures.*
import v1.initiateSupplementaryDocumentUpload.model.request.InitiateSupplementaryDocumentUploadRequestData

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future

class CreateSkeletalMetadataRecordConnectorSpec extends ConnectorSpec {

  private val nino            = "AA123456A"
  private val fileId          = "6272100a-5672-402f-9ab3-abfcb6058780"
  private val uploadTimestamp = "2026-11-18T08:53:14.597Z"
  private val taxYear         = TaxYear.fromMtd("2026-27")
  private val downstreamUrl   = url"$baseUrl/itsd/attachment-metadata/$nino/$fileId?taxYear=26-27"

  private implicit val clock: Clock = Clock.fixed(Instant.parse(uploadTimestamp), ZoneOffset.UTC)

  trait Test extends ConnectorTest {

    val connector: CreateSkeletalMetadataRecordConnector = new CreateSkeletalMetadataRecordConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val requestData: InitiateSupplementaryDocumentUploadRequestData =
      InitiateSupplementaryDocumentUploadRequestData(Nino(nino), taxYear, UserType.Individual, Some(mtdRequestBodyModel))

    val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
  }

  "CreateSkeletalMetadataRecordConnector" should {
    "return a valid response" when {
      "a valid request is made" in new HipTest with Test {
        willPost(url = downstreamUrl, body = createSkeletalMetadataRecordRequestModel).returns(Future.successful(outcome))
        await(connector.createSkeletalMetadataRecord(requestData, fileId)) shouldBe outcome
      }
    }

    "pass the correct uploadedBy value" should {
      val userTypes = Seq(
        (UserType.Individual, "individual"),
        (UserType.Organisation, "individual"),
        (UserType.Agent, "agent"),
        (UserType.SupportingAgent, "agent")
      )

      userTypes.foreach { (userType, uploadedBy) =>
        s"user type $userType is mapped to '$uploadedBy'" in new HipTest with Test {
          private val requestDataWithUserType = requestData.copy(userType = userType)
          private val bodyWithUploadedBy      = createSkeletalMetadataRecordRequestModel.copy(uploadedBy = uploadedBy)

          willPost(url = downstreamUrl, body = bodyWithUploadedBy).returns(Future.successful(outcome))
          await(connector.createSkeletalMetadataRecord(requestDataWithUserType, fileId)) shouldBe outcome
        }
      }
    }

  }

}
