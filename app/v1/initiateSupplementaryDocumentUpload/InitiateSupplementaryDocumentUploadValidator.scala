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
import api.controllers.validators.resolvers.{ResolveDetailedTaxYear, ResolveJsonObject, ResolveNino, ResolverSupport}
import api.models.auth.UserType
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.{JsObject, JsValue}
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadRulesValidator.validateBusinessRules
import v1.initiateSupplementaryDocumentUpload.model.request.*

class InitiateSupplementaryDocumentUploadValidator(nino: String, taxYear: String, userType: UserType, body: Option[JsValue])
    extends Validator[InitiateSupplementaryDocumentUploadRequestData] {

  private lazy val resolveTaxYear = ResolveDetailedTaxYear(minimumTaxYear = TaxYear.fromMtd("2026-27"))

  private lazy val resolveJson = ResolveJsonObject.resolver[InitiateSupplementaryDocumentUploadRequestBody].resolveOptionally

  def validate: Validated[Seq[MtdError], InitiateSupplementaryDocumentUploadRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body.filterNot(_ == JsObject.empty))
    ).mapN((nino, taxYear, body) => InitiateSupplementaryDocumentUploadRequestData(nino, taxYear, userType, body)) andThen
      validateBusinessRules

}
