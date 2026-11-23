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

import api.config.AppConfig
import api.controllers.*
import api.models.request.UnauthenticatedRequest
import api.utils.IdGenerator
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.*
import scala.concurrent.ExecutionContext

@Singleton
class UpscanCallbackController @Inject() (validatorFactory: UpscanCallbackValidatorFactory,
                                          service: UpscanCallbackService,
                                          cc: ControllerComponents,
                                          idGenerator: IdGenerator)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends BackendController(cc) {

  val endpointName = "upscan-callback"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "UpscanCallbackController", endpointName = endpointName)

  def handleCallback(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    implicit val unauthenticatedRequest: UnauthenticatedRequest[JsValue] = UnauthenticatedRequest(request)
    implicit val ctx: RequestContext                                     = RequestContext.from(idGenerator, endpointLogContext)

    val validator = validatorFactory.validator(request.body)

    val requestHandler =
      RequestHandler
        .withValidator(validator)
        .withService(service.handleCallback)

    requestHandler.handleRequest()
  }

}
