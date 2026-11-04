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

package v1.createAmend

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveDetailedTaxYear, ResolveNino, ResolveNonEmptyJsonObject}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import v1.createAmend.CreateAmendSupplementaryInformationRulesValidator.validateBusinessRules
import v1.createAmend.model.request.{CreateAmendSupplementaryInformationRequestBody, CreateAmendSupplementaryInformationRequestData}

import javax.inject.Inject

class CreateAmendSupplementaryInformationValidator @Inject() (nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendSupplementaryInformationRequestData] {

  private lazy val resolveTaxYear = ResolveDetailedTaxYear(minimumTaxYear = TaxYear.fromMtd("2026-27"))
  private lazy val resolveJson    = new ResolveNonEmptyJsonObject[CreateAmendSupplementaryInformationRequestBody]()

  override def validate: Validated[Seq[MtdError], CreateAmendSupplementaryInformationRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN { (validNino, validTaxYear, validBody) =>
      CreateAmendSupplementaryInformationRequestData(
        nino = validNino,
        taxYear = validTaxYear,
        body = validBody
      )
    } andThen validateBusinessRules

}
