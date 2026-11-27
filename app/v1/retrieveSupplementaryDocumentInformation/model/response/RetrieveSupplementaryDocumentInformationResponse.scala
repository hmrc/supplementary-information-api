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

import play.api.libs.json.*

case class RetrieveSupplementaryDocumentInformationResponse(documents: Seq[Document]) {

  def filteredBy(documentId: Option[String],
                 selected: Option[Boolean],
                 statuses: Seq[DocumentStatus]): RetrieveSupplementaryDocumentInformationResponse =
    copy(documents = documents.filter { document =>
      documentId.forall(_ == document.documentId) &&
      selected.forall(_ == document.selected.getOrElse(false)) &&
      (statuses.isEmpty || statuses.contains(document.status))
    })

}

object RetrieveSupplementaryDocumentInformationResponse {

  implicit val reads: Reads[RetrieveSupplementaryDocumentInformationResponse] =
    Reads.seq[Document].map(RetrieveSupplementaryDocumentInformationResponse.apply)

  implicit val writes: OWrites[RetrieveSupplementaryDocumentInformationResponse] = Json.writes[RetrieveSupplementaryDocumentInformationResponse]

}
