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

import api.controllers.validators.{AlwaysErrorsValidator, Validator}
import api.utils.UnitSpec
import play.api.libs.json.JsObject
import v1.createAmend.def1.Def1_CreateAmendSupplementaryInformationValidator
import v1.createAmend.model.request.CreateAmendSupplementaryInformationRequestData

class CreateAmendSupplementaryInformationValidatorFactorySpec extends UnitSpec {

  private def validatorFor(taxYear: String): Validator[CreateAmendSupplementaryInformationRequestData] =
    new CreateAmendSupplementaryInformationValidatorFactory().validator(
      nino = "ignoredNino",
      taxYear = taxYear,
      body = JsObject.empty
    )

  "CreateAmendTaxLiabilityAdjustmentsValidatorFactory" when {
    "given a request corresponding to a Def1 schema" should {
      "return a Def1 validator" in {
        validatorFor("2026-27") shouldBe a[Def1_CreateAmendSupplementaryInformationValidator]
      }
    }

    "given a request where no valid schema could be determined" should {
      "return a validator returning the errors" in {
        validatorFor("BAD_TAX_YEAR") shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
