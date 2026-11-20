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

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.ResolveStringPattern
import api.models.errors.{MtdError, RedirectUrlFormatError}
import cats.data.Validated
import v1.initiateSupplementaryDocumentUpload.model.request.InitiateSupplementaryDocumentUploadRequestData

object InitiateSupplementaryDocumentUploadRulesValidator extends RulesValidator[InitiateSupplementaryDocumentUploadRequestData] {

  private val redirectUrlRegex = """^https://\w+(\.\w+)+.*$""".r

  def validateBusinessRules(
      parsed: InitiateSupplementaryDocumentUploadRequestData): Validated[Seq[MtdError], InitiateSupplementaryDocumentUploadRequestData] =
    parsed.body
      .fold(valid) { body =>
        combine(
          ResolveStringPattern(body.successRedirect, redirectUrlRegex, RedirectUrlFormatError.withPath("/successRedirect")),
          ResolveStringPattern(body.errorRedirect, redirectUrlRegex, RedirectUrlFormatError.withPath("/errorRedirect"))
        )
      }
      .onSuccess(parsed)

}
