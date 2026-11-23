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

package internal.upscanCallback

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.errors.{ErrorWrapper, InternalError, RuleIncorrectOrEmptyBodyError}
import api.models.outcomes.ResponseWrapper
import internal.upscanCallback.UpscanCallbackFixtures.{successJson, successRequestData}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.test.Helpers.POST

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpscanCallbackControllerSpec extends ControllerTestRunner with MockUpscanCallbackService with MockUpscanCallbackValidatorFactory {

  "UpscanCallbackController" should {
    "return 204 (NO_CONTENT) status" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(successRequestData))
        MockUpscanCallbackService
          .handleCallback(successRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))
        runOkTest(expectedStatus = NO_CONTENT)
      }
    }

    "return validation error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(RuleIncorrectOrEmptyBodyError))
        runErrorTest(RuleIncorrectOrEmptyBodyError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(successRequestData))
        MockUpscanCallbackService
          .handleCallback(successRequestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, InternalError))))
        runErrorTest(InternalError)
      }
    }
  }

  private trait Test extends BackendControllerTest {

    protected val controller: UpscanCallbackController =
      new UpscanCallbackController(
        service = mockUpscanCallbackService,
        validatorFactory = mockUpscanCallbackValidatorFactory,
        cc = cc,
        idGenerator = mockIdGenerator
      )

    protected def callController(): Future[Result] =
      controller.handleCallback()(FakeRequest().withBody(successJson).withMethod(POST))

  }

}
