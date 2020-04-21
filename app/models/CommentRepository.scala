package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._


@Singleton
class CommentRepository @Inject()(dbConfigProvider:DatabaseConfigProvider, protected  val pR:ProductRepository,
                                  protected val uR: UserRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class CommentTableDef(tag:Tag) extends Table[Comment](tag,"comment"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def title = column[String]("description")
    def content = column[String]("content")
    def product_id = column[Int]("product_id")
    def product_fk = foreignKey("product_fk",product_id,products)(_.id,onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    def user_id = column[Int]("user_id")
    def user_fk = foreignKey("user_fk",user_id,users)(_.id,onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    def * = (id,title,content,product_id,user_id)<>((Comment.apply _).tupled,Comment.unapply)
  }
  import pR.ProductTableDef
  import uR.UserTableDef
  val comments = TableQuery[CommentTableDef]
  val products = TableQuery[ProductTableDef]
  val users    = TableQuery[UserTableDef]
  def list(): Future[Seq[Comment]] = db.run{
    comments.result
  }
  def getById(id:Int):Future[Option[Comment]] = db.run{
    comments.filter(_.id===id).result.headOption
  }
  def getByProduct(productID:Int):Future[Seq[Comment]] = db.run{
    comments.filter(_.product_id === productID).result
  }
  def create(title:String,content:String,product_id:Int,user_id:Int):Future[Comment] = db.run{
    (comments.map(c=>(c.title,c.content,c.product_id,c.user_id))
      returning comments.map(_.id)
      into{case((title,content,product_id,user_id),id)=>Comment(id,title,content,product_id,user_id)})+=(title,content,product_id,user_id)
  }
  def delete(commentId: Int):Future[Unit]= db.run{
    comments.filter(_.id===commentId).delete.map(_=>())
  }
  def update(commentId:Int,new_comment:Comment):Future[Unit] = {
    val updated_comment = new_comment.copy(commentId)
    db.run(comments.filter(_.id===commentId).update(updated_comment).map(_=>()))
  }
}
