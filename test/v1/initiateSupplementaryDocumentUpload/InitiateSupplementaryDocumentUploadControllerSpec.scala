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

import api.controllers.ControllerTestRunner
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.auth.UserType
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, InternalError, RedirectUrlFormatError}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsJson, Result}
import v1.initiateSupplementaryDocumentUpload.InitiateSupplementaryDocumentUploadFixtures.*
import v1.initiateSupplementaryDocumentUpload.model.request.InitiateSupplementaryDocumentUploadRequestData

import scala.concurrent.{ExecutionContext, Future}

class InitiateSupplementaryDocumentUploadControllerSpec
    extends ControllerTestRunner
    with MockInitiateSupplementaryDocumentUploadService
    with MockInitiateSupplementaryDocumentUploadValidatorFactory {

  private val taxYear = "2026-27"

  private val requestData =
    InitiateSupplementaryDocumentUploadRequestData(Nino(validNino), TaxYear.fromMtd(taxYear), UserType.Individual, Some(mtdRequestBodyModel))

  "InitiateSupplementaryDocumentUploadController" should {
    "return 201 (CREATED) with the response body" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedInitiateSupplementaryDocumentUploadService
          .initiateSupplementaryDocumentUpload(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponseModel))))

        runOkTestWithAudit(
          expectedStatus = CREATED,
          maybeExpectedResponseBody = Some(mtdResponseJson),
          maybeAuditRequestBody = Some(mtdRequestBodyJson)
        )
      }
    }

    "return the error as per spec" when {
      "the validator returns an error" in new Test {
        willUseValidator(returning(RedirectUrlFormatError))
        runErrorTestWithAudit(RedirectUrlFormatError, Some(mtdRequestBodyJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))
        MockedInitiateSupplementaryDocumentUploadService
          .initiateSupplementaryDocumentUpload(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, InternalError))))

        runErrorTestWithAudit(InternalError, Some(mtdRequestBodyJson))
      }
    }
  }

  private trait Test extends AuditEventChecking[GenericAuditDetail] {

    protected val controller: InitiateSupplementaryDocumentUploadController =
      new InitiateSupplementaryDocumentUploadController(
        authService = mockEnrolmentsAuthService,
        lookupService = mockMtdIdLookupService,
        validatorFactory = mockInitiateSupplementaryDocumentUploadValidatorFactory,
        service = mockInitiateSupplementaryDocumentUploadService,
        auditService = mockAuditService,
        cc = cc,
        idGenerator = mockIdGenerator
      )(ExecutionContext.global, mockAppConfig)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "InitiateSupplementaryDocumentUpload",
        transactionName = "initiate-supplementary-document-upload",
        detail = GenericAuditDetail(
          userType = UserType.Individual,
          agentReferenceNumber = None,
          versionNumber = apiVersion.name,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes().returns(Configuration("supporting-agents-access-control.enabled" -> true))
    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes().returns(false)

    protected def callController(): Future[Result] =
      controller.initiate(validNino, taxYear)(fakeRequest.withBody(AnyContentAsJson(mtdRequestBodyJson)))

  }

}
