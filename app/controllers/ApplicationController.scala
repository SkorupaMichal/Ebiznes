package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import javax.inject._
import models.services.IndexRenderService
import play.api.{Environment, Mode}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
 * The basic application controller.
 *
 * @param components The Play controller components.
 * @param silhouette The Silhouette stack.
 */
class ApplicationController @Inject()(components: ControllerComponents,
                                      silhouette: Silhouette[DefaultEnv],
                                      environment: Environment,
                                      ws: WSClient,
                                      indexRenderService: IndexRenderService,
                                      authInfoRepository: AuthInfoRepository)(implicit ec: ExecutionContext)
  extends AbstractController(components) with I18nSupport {

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, Ok)
  }


}