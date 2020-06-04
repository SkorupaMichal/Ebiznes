package models.auth.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.auth.{User, UserRoles}

import scala.concurrent.Future

trait UserService extends IdentityService[User] {

  def changeUserRole(userId: UUID, role: UserRoles.Value): Future[Boolean]

  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]]

  def createOrUpdate(loginInfo: LoginInfo,
                     email: String,
                     firstName: Option[String],
                     lastName: Option[String],
                     avatarURL: Option[String]): Future[User]
}
