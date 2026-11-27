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

package v1.retrieveSupplementaryDocumentInformation

import api.config.AppConfig
import api.controllers.validators.Validator
import v1.retrieveSupplementaryDocumentInformation.model.request.RetrieveSupplementaryDocumentInformationRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveSupplementaryDocumentInformationValidatorFactory @Inject() (implicit appConfig: AppConfig) {

  def validator(
      nino: String,
      taxYear: String,
      documentId: Option[String],
      selected: Option[String],
      status: Seq[String]
  ): Validator[RetrieveSupplementaryDocumentInformationRequestData] =
    new RetrieveSupplementaryDocumentInformationValidator(nino, taxYear, documentId, selected, status)

}
