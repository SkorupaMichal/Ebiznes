package models.daos

import slick.jdbc.JdbcProfile
import models.{User,UserRole}
import com.mohiva.play.silhouette.api.LoginInfo
trait DBTableDefinitions {
  protected val profile: JdbcProfile
  import profile.api._
  case class DBUserRole(id:Int,name:String)
  class UserRoles(tag:Tag) extends Table[DBUserRole](tag,"role"){
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
  object DBUser{
    def toUser(u:DBUser):User = User(u.userID,u.firstName,u.lastName,u.email,u.avatarURL,UserRole(u.roleId))
    def fromUser(u:User):DBUser = DBUser(u.id,u.firstName,u.lastName,u.email,u.avatarUrl,u.role.id)
  }
  class Users(tag: Tag) extends Table[DBUser](tag,"user"){
    def id = column[String]("id", O.PrimaryKey)
    def firstName = column[Option[String]]("firstName")
    def lastName = column[Option[String]]("lastName")
    def email = column[Option[String]]("email")
    def avatarURL = column[Option[String]]("avatar_url")
    def roleId = column[Int]("role_id")
    def * = (id, firstName, lastName, email, avatarURL, roleId) <> ((DBUser.apply _).tupled, DBUser.unapply)
  }
  case class DBLoginInfo(id:Option[Long],providerID:String,providerKey:String)
  object DBLoginInfo {
    def fromLoginInfo(loginInfo: LoginInfo): DBLoginInfo = DBLoginInfo(None, loginInfo.providerID, loginInfo.providerKey)
    def toLoginInfo(dbLoginInfo: DBLoginInfo) = LoginInfo(dbLoginInfo.providerID, dbLoginInfo.providerKey)
  }
}
