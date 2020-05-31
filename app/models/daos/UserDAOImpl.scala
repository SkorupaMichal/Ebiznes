package models.daos

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ExecutionContext, Future}
import com.mohiva.play.silhouette.api.LoginInfo
import models.{User,UserRoles}
import java.util.UUID

class UserDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, userRoleDAO: UserRoleDAO)(implicit ec: ExecutionContext) extends UserDAO with DAOSlick  {
  import profile.api._

  /**
   * Finds a user by its login info.
   *
   */
  def find(loginInfo: LoginInfo) = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield dbUser
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(user.userID, user.firstName, user.lastName, user.email, user.avatarURL, UserRoles(user.roleId))
      }
    }
  }
  /**
   * Finds a user by its user ID.
   *
   */
  def find(userID: UUID) = {
    val query = slickUsers.filter(_.id === userID)

    db.run(query.result.headOption).map { resultOption =>
      resultOption.map(DBUser.toUser)
    }
  }
  def save(user: User) = {
    // combine database actions to be run sequentially
    val actions = (for {
      userRoleId <- userRoleDAO.getUserRole()
      dbUser = DBUser(user.id, user.firstName, user.lastName, user.email, user.avatarUrl, user.role.id)
      _ <- slickUsers.insertOrUpdate(dbUser)
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }
  /**
   * Updates user role
   *
   */
  override def updateUserRole(userId: UUID, role: UserRoles.UserRole): Future[Boolean] = {
    db.run(slickUsers.filter(_.id === userId.toString).map(_.roleId).update(role.id)).map(_ > 0)
  }
  /**
   * Finds a user by its email
   *
   */
  def findByEmail(email: String): Future[Option[User]] = {
    db.run(slickUsers.filter(_.email === email).take(1).result.headOption).map(_ map DBUser.toUser)
  }
}
