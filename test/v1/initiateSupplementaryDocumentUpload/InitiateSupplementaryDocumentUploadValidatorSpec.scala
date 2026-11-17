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

import api.models.auth.UserType
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.utils.UnitSpec
import play.api.libs.json.{JsValue, Json}
import v1.initiateSupplementaryDocumentUpload.model.request.{
  InitiateSupplementaryDocumentUploadRequestBody,
  InitiateSupplementaryDocumentUploadRequestData
}

class InitiateSupplementaryDocumentUploadValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "someCorrelationId"

  private val nino    = "AA123456A"
  private val taxYear = "2026-27"

  private val validBody = Json.obj(
    "successRedirect" -> "https://example.com/success",
    "errorRedirect"   -> "https://example.com/error"
  )

  private def validator(nino: String = nino, taxYear: String = taxYear, body: Option[JsValue] = Some(validBody)) =
    new InitiateSupplementaryDocumentUploadValidator(nino, taxYear, UserType.Individual, body).validateAndWrapResult()

  private def error(mtdError: MtdError) = Left(ErrorWrapper(correlationId, mtdError))

  "InitiateSupplementaryDocumentUploadValidator" should {
    "return the parsed request" when {
      "both redirects are present" in {
        validator() shouldBe Right(
          InitiateSupplementaryDocumentUploadRequestData(
            Nino(nino),
            TaxYear.fromMtd(taxYear),
            UserType.Individual,
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
        validator(body = None) shouldBe Right(
          InitiateSupplementaryDocumentUploadRequestData(Nino(nino), TaxYear.fromMtd(taxYear), UserType.Individual, None)
        )
      }
      "body is empty" in {
        validator(body = Some(Json.obj())) shouldBe Right(
          InitiateSupplementaryDocumentUploadRequestData(Nino(nino), TaxYear.fromMtd(taxYear), UserType.Individual, None)
        )
      }
      Seq("successRedirect", "errorRedirect").foreach { field =>
        Seq(
          "https://a.b",
          "https://example.com:8443/path?key=value#fragment"
        ).foreach { url =>
          s"$field contains a valid redirect of $url" in {
            validator(body = Some(validBody ++ Json.obj(field -> url))).isRight shouldBe true
          }
        }

        s"$field is not present" in {
          validator(body = Some(validBody - field)).isRight shouldBe true
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
              validator(body = Some(validBody ++ Json.obj(field -> url))) shouldBe
                error(RedirectUrlFormatError.withPath(s"/$field"))
            }
          }
        }
      }
      "return a RedirectUrlFormatError" in {
        val body = Json.obj("successRedirect" -> "http://example.com", "errorRedirect" -> "invalid")

        validator(body = Some(body)) shouldBe error(RedirectUrlFormatError.withPaths(Seq("/successRedirect", "/errorRedirect")))
      }
    }
    "return a NinoFormatError" when {
      "an invalid nino is supplied" in {
        validator(nino = "A12344A") shouldBe error(NinoFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "an unsupported tax year is supplied" in {
        validator(taxYear = "2025-26") shouldBe error(RuleTaxYearNotSupportedError)
      }
    }

    "return TaxYearFormatError" when {
      "the tax year format is invalid" in {
        validator(taxYear = "25-26") shouldBe error(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "the tax year range is invalid" in {
        validator(taxYear = "2025-27") shouldBe error(RuleTaxYearRangeInvalidError)
      }
    }
  }

}
