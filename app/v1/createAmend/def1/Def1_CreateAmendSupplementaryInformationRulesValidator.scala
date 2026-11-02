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

package v1.createAmend.def1

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.ResolveStringPattern
import api.models.errors.{FormatSupplementaryInformationError, MtdError}
import cats.data.Validated
import v1.createAmend.def1.model.request.*

object Def1_CreateAmendSupplementaryInformationRulesValidator extends RulesValidator[Def1_CreateAmendSupplementaryInformationRequestData] {

  private val supplementaryInformationRegex = """^[A-Za-z0-9 &'"()*,\-./@£]{1,4000}$""".r

  private val resolveSupplementaryInformation =
    ResolveStringPattern(supplementaryInformationRegex, FormatSupplementaryInformationError)

  def validateBusinessRules(
      parsed: Def1_CreateAmendSupplementaryInformationRequestData): Validated[Seq[MtdError], Def1_CreateAmendSupplementaryInformationRequestData] = {

    validateSupplementaryInformation(parsed.body.supplementaryInformation).onSuccess(parsed)
  }

  private def validateSupplementaryInformation(supplementaryInformation: String): Validated[Seq[MtdError], String] = {
    resolveSupplementaryInformation(supplementaryInformation)
  }

}
