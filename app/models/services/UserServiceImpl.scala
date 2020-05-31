package models.services

import javax.inject.Inject
import models.daos.UserDAO
import models.daos.{LoginInfoDAO,UserDAO}
import scala.concurrent.{ExecutionContext,Future}
import com.mohiva.play.silhouette.api.LoginInfo
import models.{User,UserRoles}
import java.util.UUID

class UserServiceImpl @Inject()(userDAO:UserDAO,loginInfoDAO: LoginInfoDAO)(implicit ec: ExecutionContext) extends UserService{

  /**
   * Retrieves a user that matches the specified login info.
   *
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)
  /**
   * Retrieves a user and login info pair by userID and login info providerID
   *
   */
  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]] = {
    loginInfoDAO.find(id, providerID)
  }
  /**
   * Changes role of user
   *
   */
  override def changeUserRole(userId: UUID, role: UserRoles.Value): Future[Boolean] = {
    userDAO.updateUserRole(userId, role)
  }
  /**
   * Creates or updates user
   *
   * If a user exists for given login info or email then update the user, otherwise create a new user with the given data
   *
   */
  override def createOrUpdate(loginInfo: LoginInfo,
                              email: String,
                              firstName: Option[String],
                              lastName: Option[String],
                              avatarURL: Option[String]): Future[User] = {

    Future.sequence(Seq(userDAO.find(loginInfo), userDAO.findByEmail(email))).flatMap { users =>
      users.flatten.headOption match {
        case Some(user) =>
          userDAO.save(user.copy(
            firstName = firstName,
            lastName = lastName,
            email = Some(email),
            avatarUrl = avatarURL
          ))
        case None =>
          userDAO.save(User(
            id = UUID.randomUUID().toString,
            firstName = firstName,
            lastName = lastName,
            email = Some(email),
            avatarUrl = avatarURL,
            role = UserRoles.User
          ))
      }
    }
  }

}
