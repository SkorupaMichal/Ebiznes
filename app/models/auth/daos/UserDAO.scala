package models.auth.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.auth.{User, UserRoles}

import scala.concurrent.Future


trait UserDAO {

  def updateUserRole(userId: UUID, role: UserRoles.Value): Future[Boolean]

  def find(loginInfo: LoginInfo): Future[Option[User]]

  def find(userID: UUID): Future[Option[User]]

  def save(user: User): Future[User]

  def findByEmail(email: String): Future[Option[User]]
}
