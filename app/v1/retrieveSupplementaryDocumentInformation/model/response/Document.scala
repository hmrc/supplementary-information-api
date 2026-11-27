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

package v1.retrieveSupplementaryDocumentInformation.model.response

import api.models.domain.Timestamp
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class Document(
    documentId: String,
    documentName: Option[String],
    selected: Option[Boolean],
    selectionTimestamp: Option[Timestamp],
    status: DocumentStatus,
    uploadTimestamp: Timestamp,
    lastUpdatedTimestamp: Timestamp
)

object Document {

  implicit val reads: Reads[Document] = (
    (JsPath \ "fileId").read[String] and
      (JsPath \ "fileName").readNullable[String] and
      (JsPath \ "selected").readNullable[Boolean] and
      (JsPath \ "selectionTimestamp").readNullable[Timestamp] and
      (JsPath \ "status").read[DocumentStatus] and
      (JsPath \ "uploadTimestamp").read[Timestamp] and
      (JsPath \ "lastUpdatedTimestamp").read[Timestamp]
  )(Document.apply)

  implicit val writes: OWrites[Document] = Json.writes[Document]

}
