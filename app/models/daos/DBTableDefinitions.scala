package models.daos

import slick.jdbc.JdbcProfile
import models.{User, UserRoles}
import com.mohiva.play.silhouette.api.LoginInfo

trait DBTableDefinitions {
  protected val profile: JdbcProfile

  import profile.api._

  case class DBUserRole(id: Int, name: String)

  class UserRoles(tag: Tag) extends Table[DBUserRole](tag, "role") {
    def id = column[Int]("id", O.PrimaryKey)

    def name = column[String]("name")

    def * = (id, name) <> (DBUserRole.tupled, DBUserRole.unapply)
  }

  case class DBUser(userID: String,
                    firstName: Option[String],
                    lastName: Option[String],
                    email: Option[String],
                    avatarURL: Option[String],
                    roleId: Int)

  object DBUser {
    def toUser(u: DBUser): User = User(u.userID, u.firstName, u.lastName, u.email, u.avatarURL, UserRoles(u.roleId))

    def fromUser(u: User): DBUser = DBUser(u.id, u.firstName, u.lastName, u.email, u.avatarUrl, u.role.id)
  }

  class Users(tag: Tag) extends Table[DBUser](tag, "user") {
    def id = column[String]("id", O.PrimaryKey)

    def firstName = column[Option[String]]("firstName")

    def lastName = column[Option[String]]("lastName")

    def email = column[Option[String]]("email")

    def avatarURL = column[Option[String]]("avatar_url")

    def roleId = column[Int]("role_id")

    def * = (id, firstName, lastName, email, avatarURL, roleId) <> ((DBUser.apply _).tupled, DBUser.unapply)
  }

  case class DBLoginInfo(id: Option[Long], providerID: String, providerKey: String)

  object DBLoginInfo {
    def fromLoginInfo(loginInfo: LoginInfo): DBLoginInfo = DBLoginInfo(None, loginInfo.providerID, loginInfo.providerKey)

    def toLoginInfo(dbLoginInfo: DBLoginInfo) = LoginInfo(dbLoginInfo.providerID, dbLoginInfo.providerKey)
  }

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "logininfo") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def providerID = column[String]("provider_id")

    def providerKey = column[String]("provider_key")

    def * = (id.?, providerID, providerKey) <> ((DBLoginInfo.apply _).tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo(userID: String, loginInfoId: Long)
  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, Some("auth"), "userlogininfo") {
    def userID = column[String]("user_id")
    def loginInfoId = column[Long]("login_info_id")
    def * = (userID, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  case class DBOAuth2Info(id: Option[Long], accessToken: String, tokenType: Option[String], expiresIn: Option[Int], refreshToken: Option[String], loginInfoId: Long)

  class OAuth2Infos(tag: Tag) extends Table[DBOAuth2Info](tag, Some("auth"), "oauth2info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accessToken = column[String]("access_token")
    def tokenType = column[Option[String]]("token_type")
    def expiresIn = column[Option[Int]]("expires_in")
    def refreshToken = column[Option[String]]("refresh_token")
    def loginInfoId = column[Long]("login_info_id")
    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId) <> (DBOAuth2Info.tupled, DBOAuth2Info.unapply)
  }
  val slickUsers = TableQuery[Users]
  val slickUserRoles = TableQuery[UserRoles]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]

  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) =
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
}
