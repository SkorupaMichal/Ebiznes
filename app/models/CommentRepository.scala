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
    def title = column[String]("title")
    def content = column[String]("content")
    def productId = column[Int]("product_id")
    def productFk = foreignKey("product_fk",productId,products)(_.id,onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    def userId = column[Int]("user_id")
    def userFk = foreignKey("user_fk",userId,users)(_.id,onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    def * = (id,title,content,productId,userId)<>((Comment.apply _).tupled,Comment.unapply)
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
    comments.filter(_.productId === productID).result
  }
  def getWithProductDesc(productID:Int):Future[Seq[(Int, String, String, Int, String, Int, String)]] = db.run{
    val jsontable = comments join products on{
      case (comm,prod) =>
        prod.id === productID &&
        comm.productId === prod.id
    }
    val query = for{
      (comm,prod) <- jsontable
    }yield(comm.id,comm.title,comm.content,prod.id,prod.name,prod.cost,prod.producer)
    query.result
  }
  def getByUser(userId:Int): Future[Seq[Comment]] = db.run{
    comments.filter(_.userId === userId).result
  }
  def create(title:String,content:String,prodId:Int,userId:Int):Future[Comment] = db.run{
    (comments.map(c=>(c.title,c.content,c.productId,c.userId))
      returning comments.map(_.id)
      into{case((title,content,prodId,userId),id)=>Comment(id,title,content,prodId,userId)})+=(title,content,prodId,userId)
  }
  def delete(commentId: Int):Future[Unit]= db.run{
    comments.filter(_.id===commentId).delete.map(_=>())
  }
  def deleteByProductId(productId: Int):Future[Unit] = db.run{
    comments.filter(_.productId === productId).delete.map(_=>())
  }
  def update(commentId:Int,newComment:Comment):Future[Unit] = {
    val updatedComment = newComment.copy(commentId)
    db.run(comments.filter(_.id===commentId).update(updatedComment).map(_=>()))
  }
}
