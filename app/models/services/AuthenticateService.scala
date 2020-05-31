package models.services
import java.time.Instant
import java.util.UUID

import akka.actor.ActorRef
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import com.mohiva.play.silhouette.impl.providers._
import javax.inject.{Inject, Named}
import models.User
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import models.daos.LoginInfoDAO
import scala.concurrent.{ExecutionContext, Future}


class AuthenticateService @Inject()(credentialsProvider: CredentialsProvider,
                                    userService: UserService,
                                    authInfoRepository: AuthInfoRepository,
                                    loginInfoDAO: LoginInfoDAO,
                                    socialProviderRegistry: SocialProviderRegistry,
                                    )(implicit ec: ExecutionContext) {
  implicit val timeout: Timeout = 5.seconds

  /**
   * Creates or fetches existing user for given social profile and binds it with given auth info
   *
   */
  def provideUserForSocialAccount[T <: AuthInfo](provider: String, profile: CommonSocialProfile, authInfo: T): Future[UserForSocialAccountResult] = {
    profile.email match {
      case Some(email) =>
        loginInfoDAO.getAuthenticationProviders(email).flatMap { providers =>
          if (providers.contains(provider) || providers.isEmpty) {
            for {
              user <- userService.createOrUpdate(
                profile.loginInfo,
                email,
                profile.firstName,
                profile.lastName,
                profile.avatarURL
              )
              _ <- addAuthenticateMethod(UUID.fromString(user.id), profile.loginInfo, authInfo)
            } yield AccountBound(user)
          } else {
            Future.successful(EmailIsBeingUsed(providers))
          }
        }
      case None =>
        Future.successful(NoEmailProvided)
    }
  }
  /**
   * Adds authentication method to user
   *
   */
  def addAuthenticateMethod[T <: AuthInfo](userId: UUID, loginInfo: LoginInfo, authInfo: T): Future[Unit] = {
    for {
      _ <- loginInfoDAO.saveUserLoginInfo(userId, loginInfo)
      _ <- authInfoRepository.add(loginInfo, authInfo)
    } yield ()
  }
  /**
   * Checks whether user have authentication method for given provider id
   *
   */
  def userHasAuthenticationMethod(userId: UUID, providerId: String): Future[Boolean] = {
    loginInfoDAO.find(userId, providerId).map(_.nonEmpty)
  }

  /**
   * Get list of providers of user authentication methods
   *
   */
  def getAuthenticationProviders(email: String): Future[Seq[String]] = loginInfoDAO.getAuthenticationProviders(email)
}
sealed trait AuthenticateResult
case class Success(user: User) extends AuthenticateResult
case class InvalidPassword(attemptsAllowed: Int) extends AuthenticateResult
object NonActivatedUserEmail extends AuthenticateResult
object UserNotFound extends AuthenticateResult
case class ToManyAuthenticateRequests(nextAllowedAttemptTime: Instant) extends AuthenticateResult
sealed trait UserForSocialAccountResult
case object NoEmailProvided extends UserForSocialAccountResult
case class EmailIsBeingUsed(providers: Seq[String]) extends UserForSocialAccountResult
case class AccountBound(user: User) extends UserForSocialAccountResult
