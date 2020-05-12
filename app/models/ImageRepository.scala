package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._


class ImageRepository @Inject()(dbConfigProvider:DatabaseConfigProvider,protected val pR:ProductRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  class ImageTableDef(tag:Tag) extends Table[Image](tag,"image"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def url = column[String]("url")
    def description = column[String]("description",O.Default(""))
    def productId = column[Int]("product_id")
    def productFk = foreignKey("product_fk",productId,products)(_.id,
      onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,url,description,productId) <>((Image.apply _).tupled,Image.unapply)
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
  def getByProductId(prodID:Int):Future[Seq[Image]] = db.run{
    images.filter(_.productId === prodID).result
  }
  def create(url:String,description:String,prodId:Int):Future[Image] = db.run{
    (images.map(c=>(c.url,c.description,c.productId))
      returning images.map(_.id)
      into{case((url,description,prodId),id)=>Image(id,url,description,prodId)})+=(url,description,prodId)
  }
  def delete(imageId:Int):Future[Unit] = {
    db.run(images.filter(_.id ===imageId).delete).map(_=>())
  }
  def update(imageID:Int,newImage:Image):Future[Unit]={
    val updatedIamge = newImage.copy(imageID)
    db.run(images.filter(_.id === imageID).update(updatedIamge)).map(_=>())
  }
}
