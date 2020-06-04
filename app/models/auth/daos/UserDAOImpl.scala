package models.auth.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.auth.{User, UserRoles}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}


class UserDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, userRoleDAO: UserRoleDAO)
                           (implicit ec: ExecutionContext) extends UserDAO with DAOSlick {

  import profile.api._
  def list(): Future[Seq[User]] = {
    db.run(slickUsers.result).map(users=>{
      users.map(DBUser.toUser)
    })
  }
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

  def find(userID: UUID) = {
    val query = slickUsers.filter(_.id === userID.toString)

    db.run(query.result.headOption).map { resultOption =>
      resultOption.map(DBUser.toUser)
    }
  }

  def save(user: User) = db.run {
    val dbUser = DBUser(user.userID, user.firstName, user.lastName, user.email, user.avatarURL, user.role.id)
    slickUsers.insertOrUpdate(dbUser)

  }.map(_ => user)

  override def updateUserRole(userId: UUID, role: UserRoles.UserRole): Future[Boolean] = {
    db.run(slickUsers.filter(_.id === userId.toString).map(_.roleId).update(role.id)).map(_ > 0)
  }

  def findByEmail(email: String): Future[Option[User]] = {
    db.run(slickUsers.filter(_.email === email).take(1).result.headOption).map(_ map DBUser.toUser)
  }
}
