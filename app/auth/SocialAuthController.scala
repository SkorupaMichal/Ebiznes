package auth

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{Logger, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, SocialProvider, SocialProviderRegistry}
import javax.inject.Inject
import models.auth.services.{AccountBound, AuthenticateService, EmailIsBeingUsed, NoEmailProvided, UserService}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, ControllerComponents, Request}
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class SocialAuthController @Inject()(
                                     silhouette: Silhouette[DefaultEnv],
                                     configuration: Configuration,
                                     clock: Clock,
                                     authenticateService: AuthenticateService,
                                     socialProviderRegistry: SocialProviderRegistry)
                                    (implicit ex: ExecutionContext) extends AbstractAuthController(silhouette, configuration, clock) with Logger {

  def authenticate(provider: String) = Action.async { implicit request: Request[AnyContent] =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            userBindResult <- authenticateService.provideUserForSocialAccount(provider, profile, authInfo)
            result <- userBindResult match {
              case AccountBound(u) =>
                authenticateUser(u, profile.loginInfo, rememberMe = true)
              case EmailIsBeingUsed(providers) =>
                Future.successful(Conflict(Json.obj("error" -> "EmailIsBeingUsed", "providers" -> providers)))
              case NoEmailProvided =>
                Future.successful(BadRequest(Json.obj("error" -> "NoEmailProvided")))
            }
          } yield result
        }
      case _ => Future.failed(new ProviderException("Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect("/error?message=socialAuthFailed")
    }
  }
}
