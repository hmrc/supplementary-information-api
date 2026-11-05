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

package v1.retrieveSupplementaryInformation

import api.controllers.ControllerTestRunner
import api.models.domain.TaxYear
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.mvc.Result
import v1.retrieveSupplementaryInformation.RetrieveSupplementaryInformationFixtures.*
import v1.retrieveSupplementaryInformation.model.request.RetrieveSupplementaryInformationRequestData

import scala.concurrent.{ExecutionContext, Future}

class RetrieveSupplementaryInformationControllerSpec
    extends ControllerTestRunner
    with MockRetrieveSupplementaryInformationService
    with MockRetrieveSupplementaryInformationValidatorFactory {

  private val taxYear     = "2026-27"
  private val requestData = RetrieveSupplementaryInformationRequestData(parsedNino, TaxYear.fromMtd(taxYear))

  "RetrieveSupplementaryInformationController" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveSupplementaryInformationService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveSupplementaryInformationService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller: RetrieveSupplementaryInformationController = new RetrieveSupplementaryInformationController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveSupplementaryInformationValidatorFactory,
      service = mockRetrieveSupplementaryInformationService,
      cc = cc,
      idGenerator = mockIdGenerator
    )(ExecutionContext.global, mockAppConfig)

    MockedAppConfig.featureSwitchConfig
      .returns(Configuration("supporting-agents-access-control.enabled" -> true))
      .anyNumberOfTimes()

    MockedAppConfig
      .endpointAllowsSupportingAgents(controller.endpointName)
      .returns(false)
      .anyNumberOfTimes()

    protected def callController(): Future[Result] =
      controller.retrieve(validNino, taxYear)(fakeGetRequest)

  }

}
