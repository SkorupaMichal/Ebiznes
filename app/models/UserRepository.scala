package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

@Singleton
class UserRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  /*
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class UserTableDef(tag:Tag) extends Table[User](tag,"user"){
    /*User(id:String, firstName:Option[String], lastName:Option[String], email:Option[String],  avatarUrl:Option[String], role:String = "user")*/
    def id = column[String]("id",O.PrimaryKey,O.AutoInc)
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def email = column[String]("email")
    def avatarUrl = column[String]("firstName")
    def role = column[String]("firstName")
    def * = (id,firstName,lastName,email,avatarUrl)<>((User.apply _).tupled,User.unapply)
  }

  val users = TableQuery[UserTableDef]
  def list(): Future[Seq[User]] = db.run{
    users.result
  }
  /*
  def getById(id:Int):Future[Option[User]] = db.run{
    users.filter(_.id===id).result.headOption
  }
  def getByLogin(login:String): Future[Option[User]] = db.run{
    users.filter(_.login === login).result.headOption
  }
  def checkUserLogin(login:String) = db.run{
    users.filter(_.login === login).length.result
  }
  def create(login:String,email:String,password:String):Future[User] = db.run{
    (users.map(c=>(c.login,c.email,c.password))
      returning users.map(_.id)
      into{case((login,email,password),id)=>User(id,login,email,password)})+=(login,email,password)
  }
  def delete(productSetId: Int):Future[Unit]= db.run{
    users.filter(_.id===productSetId).delete.map(_=>())
  }
  def update(userId:Int,newUser:User):Future[Unit] = {
    val updatedUser = newUser.copy(userId)
    db.run(users.filter(_.id===userId).update(updatedUser).map(_=>()))
  }*/*/
}
