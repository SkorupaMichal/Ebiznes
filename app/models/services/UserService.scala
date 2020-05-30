package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.services.IdentityService
import models.{User, UserRole}
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.LoginInfo
trait UserService extends IdentityService[User] {

  def changeUserRole(userId: UUID, role: UserRole.Value): Future[Boolean]

  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]]

  def createOrUpdate(loginInfo: LoginInfo, email: String, firstName: Option[String], lastName: Option[String], avatarURL: Option[String]): Future[User]

  def setEmailActivated(user: User): Future[User]
}
