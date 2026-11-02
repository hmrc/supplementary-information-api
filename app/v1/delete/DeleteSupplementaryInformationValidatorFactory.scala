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

package v1.delete

import api.controllers.validators.Validator
import cats.data.Validated.{Invalid, Valid}
import v1.delete.def1.Def1_DeleteSupplementaryInformationValidator
import v1.delete.model.request.DeleteSupplementaryInformationRequestData

import javax.inject.Singleton

@Singleton
class DeleteSupplementaryInformationValidatorFactory {

  def validator(nino: String, taxYear: String): Validator[DeleteSupplementaryInformationRequestData] =
    DeleteSupplementaryInformationSchema.schemaFor(taxYear) match {
      case Valid(DeleteSupplementaryInformationSchema.Def1) =>
        new Def1_DeleteSupplementaryInformationValidator(nino, taxYear)
      case Invalid(errors) =>
        Validator.returningErrors(errors)
    }
}