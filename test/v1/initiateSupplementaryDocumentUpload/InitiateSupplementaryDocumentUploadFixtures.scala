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

import api.models.domain.{Nino, TaxYear}
import play.api.libs.json.{JsObject, Json}
import v1.initiateSupplementaryDocumentUpload.model.*
import v1.initiateSupplementaryDocumentUpload.model.request.{
  InitiateSupplementaryDocumentUploadRequestBody,
  InitiateSupplementaryDocumentUploadRequestData,
  UpscanInitiateRequest
}
import v1.initiateSupplementaryDocumentUpload.model.response.{InitiateSupplementaryDocumentUploadResponse, UploadRequest}

object InitiateSupplementaryDocumentUploadFixtures {

  val mtdRequestBodyJson: JsObject = Json.obj(
    "successRedirect" -> "https://example.com/success",
    "errorRedirect"   -> "https://example.com/error"
  )

  val downstreamRequestBodyJson: JsObject = Json.obj(
    "callbackUrl"     -> "placeholder",
    "successRedirect" -> "https://example.com/success",
    "errorRedirect"   -> "https://example.com/error",
    "maximumFileSize" -> 5242880L
  )

  val downstreamResponseJson: JsObject = Json.obj(
    "reference" -> "document-reference",
    "uploadRequest" -> Json.obj(
      "href" -> "https://example.com/upload",
      "fields" -> Json.obj(
        "key"             -> "document-reference",
        "x-amz-algorithm" -> "AWS4-HMAC-SHA256"
      )
    )
  )

  val mtdResponseJson: JsObject = Json.obj(
    "documentId" -> "document-reference",
    "uploadRequest" -> Json.obj(
      "uploadUrl" -> "https://example.com/upload",
      "fields" -> Json.obj(
        "key"             -> "document-reference",
        "x-amz-algorithm" -> "AWS4-HMAC-SHA256"
      )
    )
  )

  val requestBodyModel: InitiateSupplementaryDocumentUploadRequestBody =
    InitiateSupplementaryDocumentUploadRequestBody(
      successRedirect = Some("https://example.com/success"),
      errorRedirect = Some("https://example.com/error")
    )

  val requestDataModel: InitiateSupplementaryDocumentUploadRequestData =
    InitiateSupplementaryDocumentUploadRequestData(
      nino = Nino("AA123456A"),
      taxYear = TaxYear.fromMtd("2026-27"),
      body = Some(requestBodyModel)
    )

  val upscanRequestModel: UpscanInitiateRequest =
    UpscanInitiateRequest(
      callbackUrl = "placeholder",
      successRedirect = Some("https://example.com/success"),
      errorRedirect = Some("https://example.com/error"),
      maximumFileSize = 5242880
    )

  val responseModel: InitiateSupplementaryDocumentUploadResponse =
    InitiateSupplementaryDocumentUploadResponse(
      documentId = "document-reference",
      uploadRequest = UploadRequest(
        uploadUrl = "https://example.com/upload",
        fields = Map("key" -> "document-reference", "x-amz-algorithm" -> "AWS4-HMAC-SHA256")
      )
    )

}
