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

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveDetailedTaxYear, ResolveJsonObject, ResolveNino}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.{JsObject, JsValue}
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadRulesValidator.validateBusinessRules
import v1.initiateSupplementaryDocumentUpload.model.request.*

class InitiateSupplementaryDocumentUploadValidator(nino: String, taxYear: String, body: Option[JsValue])
    extends Validator[InitiateSupplementaryDocumentUploadRequestData] {

  private lazy val resolveTaxYear = ResolveDetailedTaxYear(minimumTaxYear = TaxYear.fromMtd("2026-27"))

  private lazy val resolveJsonObject = new ResolveJsonObject[InitiateSupplementaryDocumentUploadRequestBody]()

  private def resolveJson(data: JsValue): Validated[Seq[MtdError], Option[InitiateSupplementaryDocumentUploadRequestBody]] =
    data match {
      case obj: JsObject if obj.fields.isEmpty => Validated.Valid(None)
      case _                                   => resolveJsonObject(data).map(Some(_))
    }

  def validate: Validated[Seq[MtdError], InitiateSupplementaryDocumentUploadRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      body.map(resolveJson).getOrElse(Validated.Valid(None))
    ).mapN((nino, taxYear, body) => InitiateSupplementaryDocumentUploadRequestData(nino, taxYear, body)) andThen
      validateBusinessRules

}
