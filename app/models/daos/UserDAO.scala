package models.daos

import java.util.UUID
import com.mohiva.play.silhouette.api.LoginInfo
import models.{User,UserRoles}
import scala.concurrent.Future
/**
 * Give access to the user object.
 */
trait UserDAO {

  def list(): Future[Seq[User]]
  /**
   * Updates user role
   *
   */
  def updateUserRole(userId: UUID, role: UserRoles.Value): Future[Boolean]
  /**
   * Finds a user by its login info.
   *
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]
  /**
   * Finds a user by its user ID.
   *
   */
  def find(userID: UUID): Future[Option[User]]
  /**
   * Saves a user.
   *
   */
  def save(user: User): Future[User]

  /**
   * Finds a user by its email
   *
   */
  def findByEmail(email: String): Future[Option[User]]
}
