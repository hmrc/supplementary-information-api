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
import api.models.errors.*
import api.utils.UnitSpec
import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.{JsValue, Json}
import v1.initiateSupplementaryDocumentUpload.model.request.{
  InitiateSupplementaryDocumentUploadRequestBody,
  InitiateSupplementaryDocumentUploadRequestData
}

class InitiateSupplementaryDocumentUploadValidatorSpec extends UnitSpec {

  private val nino    = "AA123456A"
  private val taxYear = "2026-27"

  private val validBody = Json.obj(
    "successRedirect" -> "https://example.com/success",
    "errorRedirect"   -> "https://example.com/error"
  )

  private def validator(body: Option[JsValue] = Some(validBody)) =
    new InitiateSupplementaryDocumentUploadValidator(nino, taxYear, body)

  "InitiateSupplementaryDocumentUploadValidator" should {
    "return the parsed request" when {
      "both redirects are present" in {
        validator().validate shouldBe Valid(
          InitiateSupplementaryDocumentUploadRequestData(
            Nino(nino),
            TaxYear.fromMtd(taxYear),
            Some(
              InitiateSupplementaryDocumentUploadRequestBody(
                Some("https://example.com/success"),
                Some("https://example.com/error")
              )
            )
          )
        )
      }
      "body is not present" in {
        validator(None).validate shouldBe Valid(
          InitiateSupplementaryDocumentUploadRequestData(Nino(nino), TaxYear.fromMtd(taxYear), None)
        )
      }
      Seq("successRedirect", "errorRedirect").foreach { field =>
        Seq(
          "https://a.b",
          "https://example.com:8443/path?key=value#fragment"
        ).foreach { url =>
          s"$field contains a valid redirect of $url" in {
            validator(Some(validBody ++ Json.obj(field -> url))).validate.isValid shouldBe true
          }
        }

        s"$field is not present" in {
          validator(Some(validBody - field)).validate.isValid shouldBe true
        }
      }
    }

    "return an error" when {
      "a redirect is present but not a valid URL" when {
        Seq("successRedirect", "errorRedirect").foreach { field =>
          Seq(
            "",
            " ",
            "http://example.com",
            "HTTPS://example.com",
            "www.example.com",
            "https://example",
            "https://example."
          ).foreach { url =>
            s"reject $field not matching the regex: '$url'" in {
              validator(Some(validBody ++ Json.obj(field -> url))).validate shouldBe
                Invalid(List(RedirectUrlFormatError.withPath(s"/$field")))
            }
          }
        }
      }
      "both redirects are invalid" in {
        implicit val correlationId: String = "someCorrelationId"
        val body                           = Json.obj("successRedirect" -> "http://example.com", "errorRedirect" -> "invalid")

        validator(Some(body)).validateAndWrapResult() shouldBe Left(
          ErrorWrapper(correlationId, RedirectUrlFormatError.withPaths(Seq("/successRedirect", "/errorRedirect")))
        )
      }
    }
  }

}
