package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

case class Image(id:Int,url:String,description:String,product_id:Int)
object Image{
  implicit val imageForm = Json.format[Image]
}
class ImageRepository @Inject()(dbConfigProvider:DatabaseConfigProvider,protected val pR:ProductRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  class ImageTableDef(tag:Tag) extends Table[Image](tag,"image"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def url = column[String]("url")
    def description = column[String]("description",O.Default(""))
    def product_id = column[Int]("product_id")
    def product_fk = foreignKey("product_fk",product_id,products)(_.id,
      onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,url,description,product_id) <>((Image.apply _).tupled,Image.unapply)
  }
  import pR.ProductTableDef
  val images = TableQuery[ImageTableDef]
  val products = TableQuery[ProductTableDef]

  def list(): Future[Seq[Image]] = db.run{
    images.result
  }
  def getById(id:Int):Future[Option[Image]] = db.run{
    images.filter(_.id === id).result.headOption
  }
  def create(url:String,description:String,product_id:Int):Future[Image] = db.run{
    (images.map(c=>(c.url,c.description,c.product_id))
      returning images.map(_.id)
      into{case((url,description,product_id),id)=>Image(id,url,description,product_id)})+=(url,description,product_id)
  }
  def delete(imageId:Int):Future[Unit] = {
    db.run(images.filter(_.id ===imageId).delete).map(_=>())
  }
  def update(imageID:Int,new_Image:Image):Future[Unit]={
    val updatedIamge = new_Image.copy(imageID)
    db.run(images.filter(_.id === imageID).update(updatedIamge)).map(_=>())
  }
}
