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

import api.utils.UnitSpec
import play.api.libs.json.{JsError, JsValue, Json}
import v1.retrieveSupplementaryDocumentInformation.RetrieveSupplementaryDocumentInformationFixtures.*

class RetrieveSupplementaryDocumentInformationResponseSpec extends UnitSpec {

  private val emptyArrayJson: JsValue = Json.parse("[]")

  private val missingFileIdJson: JsValue = Json.parse(
    """[{"status": "success", "uploadTimestamp": "2026-08-24T14:10:22Z", "lastUpdatedTimestamp": "2026-08-24T14:12:22Z"}]"""
  )

  private val missingLastUpdatedTimestampJson: JsValue = Json.parse(
    """[{"fileId": "abc", "status": "success", "uploadTimestamp": "2026-08-24T14:10:22Z"}]"""
  )

  private val invalidStatusJson: JsValue = Json.parse(
    """[{"fileId": "abc", "status": "failedAcknowledged", "uploadTimestamp": "2026-08-24T14:10:22Z", "lastUpdatedTimestamp": "2026-08-24T14:12:22Z"}]"""
  )

  "RetrieveSupplementaryDocumentInformationResponse" when {
    "read from valid downstream JSON" should {
      "produce the expected object" in {
        downstreamResponseJson.as[RetrieveSupplementaryDocumentInformationResponse] shouldBe responseModel
      }
    }

    "read from an empty downstream array" should {
      "produce an object with no documents" in {
        emptyArrayJson.as[RetrieveSupplementaryDocumentInformationResponse] shouldBe RetrieveSupplementaryDocumentInformationResponse(Nil)
      }
    }

    "read from downstream JSON without a fileId" should {
      "provide a JsError" in {
        missingFileIdJson.validate[RetrieveSupplementaryDocumentInformationResponse] shouldBe a[JsError]
      }
    }

    "read from downstream JSON without a lastUpdatedTimestamp" should {
      "provide a JsError" in {
        missingLastUpdatedTimestampJson.validate[RetrieveSupplementaryDocumentInformationResponse] shouldBe a[JsError]
      }
    }

    "read from downstream JSON with an unknown status" should {
      "provide a JsError" in {
        invalidStatusJson.validate[RetrieveSupplementaryDocumentInformationResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected MTD JSON" in {
        Json.toJson(responseModel) shouldBe mtdResponseJson
      }
    }

    "filteredBy is called" should {
      "return all documents" when {
        "no filters are supplied" in {
          responseModel.filteredBy(None, None, Nil) shouldBe responseModel
        }
      }

      "return only the matching document" when {
        "a documentId is supplied" in {
          responseModel.filteredBy(Some(requestedDocument.documentId), None, Nil).documents shouldBe List(requestedDocument)
        }

        "selected=true is supplied" in {
          responseModel.filteredBy(None, Some(true), Nil).documents shouldBe List(successDocument)
        }

        "selected=false is supplied (a missing selected flag counts as false)" in {
          responseModel.filteredBy(None, Some(false), Nil).documents shouldBe List(requestedDocument)
        }

        "a status is supplied" in {
          responseModel.filteredBy(None, None, List(DocumentStatus.success)).documents shouldBe List(successDocument)
        }
      }

      "return every document matching any of the statuses" when {
        "multiple statuses are supplied" in {
          responseModel.filteredBy(None, None, List(DocumentStatus.success, DocumentStatus.requested)) shouldBe responseModel
        }
      }

      "return no documents" when {
        "nothing matches" in {
          responseModel.filteredBy(None, None, List(DocumentStatus.failed)).documents shouldBe empty
        }

        "the filters contradict each other" in {
          responseModel.filteredBy(Some(successDocument.documentId), Some(false), Nil).documents shouldBe empty
        }
      }
    }
  }

}
