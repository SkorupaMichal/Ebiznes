package models.auth.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.auth.{User, UserRoles}
import models.auth.daos.{LoginInfoDAO, UserDAO}

import scala.concurrent.{ExecutionContext, Future}


class UserServiceImpl @Inject()(userDAO: UserDAO,
                                loginInfoDAO: LoginInfoDAO)(implicit ec: ExecutionContext) extends UserService {

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]] = {
    loginInfoDAO.find(id, providerID)
  }

  override def changeUserRole(userId: UUID, role: UserRoles.Value): Future[Boolean] = {
    userDAO.updateUserRole(userId, role)
  }

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
            avatarURL = avatarURL
          ))
        case None =>
          userDAO.save(User(
            userID = UUID.randomUUID().toString,
            firstName = firstName,
            lastName = lastName,
            email = Some(email),
            avatarURL = avatarURL,
            role = UserRoles.User
          ))
      }
    }
  }
}
