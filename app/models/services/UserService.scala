package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.services.IdentityService
import models.{User, UserRoles}
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.LoginInfo
trait UserService extends IdentityService[User] {

  /**
   * Changes role of user
   *
   */
  def changeUserRole(userId: UUID, role: UserRoles.Value): Future[Boolean]

  /**
   * Retrieves a user and login info pair by userID and login info providerID
   *
   */
  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]]

  /**
   * Creates or updates a user
   *
   * If a user exists for given login info or email then update the user, otherwise create a new user with the given data
   *
   */
  def createOrUpdate(loginInfo: LoginInfo,
                     email: String,
                     firstName: Option[String],
                     lastName: Option[String],
                     avatarURL: Option[String]): Future[User]

}
