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

import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import internal.upscanCallback.UpscanCallbackFixtures.successRequestBody
import internal.upscanCallback.model.UpscanCallbackRequestData

import scala.concurrent.Future

class UpscanCallbackServiceSpec extends ServiceSpec {

  val requestData: UpscanCallbackRequestData = UpscanCallbackRequestData(
    body = successRequestBody
  )

  "UpscanCallbackService" should {

    "return a success response" in new Test {
      val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
      MockUpscanCallbackConnector.handleCallback(requestData).returns(Future.successful(outcome))

      await(service.handleCallback(requestData)) shouldBe outcome
    }

    "map errors according to spec" when {

      def serviceError(downStreamErrorCode: String, error: MtdError): Unit =
        s"a $downStreamErrorCode error is returned from the service" in new Test {

          MockUpscanCallbackConnector
            .handleCallback(requestData)
            .returns(
              Future.successful(
                Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downStreamErrorCode))))
              )
            )

          await(service.handleCallback(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errorMap = List(
        "1000" -> InternalError,
        "1289" -> InternalError,
        "5010" -> InternalError
      )

      errorMap.foreach(args => serviceError.tupled(args))
    }
  }

  trait Test extends MockUpscanCallbackConnector {
    val service = new UpscanCallbackService(mockUpscanCallbackConnector)
  }

}
