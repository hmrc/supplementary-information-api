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

import api.models.domain.Timestamp
import play.api.libs.json.{JsValue, Json}
import v1.retrieveSupplementaryDocumentInformation.model.response.{Document, DocumentStatus, RetrieveSupplementaryDocumentInformationResponse}

object RetrieveSupplementaryDocumentInformationFixtures {

  // What HIP returns (bare array, HIP field names, extra fields we don't expose, no milliseconds)
  val downstreamResponseJson: JsValue = Json.parse(
    """
      |[
      |  {
      |    "fileId": "fe4440a8-13dd-4f9e-961a-f8b8c5b3872d",
      |    "fileName": "receipt.pdf",
      |    "fileSize": 12345,
      |    "scanId": "scan-123",
      |    "selected": true,
      |    "selectionTimestamp": "2026-08-24T14:15:22Z",
      |    "status": "success",
      |    "storeId": "store-123",
      |    "storedName": "stored-receipt.pdf",
      |    "uploadTimestamp": "2026-08-24T14:10:22Z",
      |    "lastUpdatedTimestamp": "2026-08-24T14:12:22Z",
      |    "uploadedBy": "individual"
      |  },
      |  {
      |    "fileId": "11370e18-6e24-453e-b45a-76d3e32ea33d",
      |    "status": "requested",
      |    "uploadTimestamp": "2026-08-25T09:00:00Z",
      |    "lastUpdatedTimestamp": "2026-08-25T09:05:00Z"
      |  }
      |]
    """.stripMargin
  )

  // What MTD returns (wrapped in "documents", MTD field names, milliseconds)
  val mtdResponseJson: JsValue = Json.parse(
    """
      |{
      |  "documents": [
      |    {
      |      "documentId": "fe4440a8-13dd-4f9e-961a-f8b8c5b3872d",
      |      "documentName": "receipt.pdf",
      |      "selected": true,
      |      "selectionTimestamp": "2026-08-24T14:15:22.000Z",
      |      "status": "success",
      |      "uploadTimestamp": "2026-08-24T14:10:22.000Z",
      |      "lastUpdatedTimestamp": "2026-08-24T14:12:22.000Z"
      |    },
      |    {
      |      "documentId": "11370e18-6e24-453e-b45a-76d3e32ea33d",
      |      "status": "requested",
      |      "uploadTimestamp": "2026-08-25T09:00:00.000Z",
      |      "lastUpdatedTimestamp": "2026-08-25T09:05:00.000Z"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val responseModel: RetrieveSupplementaryDocumentInformationResponse =
    RetrieveSupplementaryDocumentInformationResponse(
      documents = List(
        Document(
          documentId = "fe4440a8-13dd-4f9e-961a-f8b8c5b3872d",
          documentName = Some("receipt.pdf"),
          selected = Some(true),
          selectionTimestamp = Some(Timestamp("2026-08-24T14:15:22.000Z")),
          status = DocumentStatus.success,
          uploadTimestamp = Timestamp("2026-08-24T14:10:22.000Z"),
          lastUpdatedTimestamp = Timestamp("2026-08-24T14:12:22.000Z")
        ),
        Document(
          documentId = "11370e18-6e24-453e-b45a-76d3e32ea33d",
          documentName = None,
          selected = None,
          selectionTimestamp = None,
          status = DocumentStatus.requested,
          uploadTimestamp = Timestamp("2026-08-25T09:00:00.000Z"),
          lastUpdatedTimestamp = Timestamp("2026-08-25T09:05:00.000Z")
        )
      )
    )

  val successDocument: Document   = responseModel.documents.head
  val requestedDocument: Document = responseModel.documents(1)

}
