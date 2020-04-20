package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

case class Product(id:Int,name:String,count:Int,producer:String,subcategory_id:Int)
object Product{
  implicit val productForm = Json.format[Product]
}
class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                  protected val cR: SubCategoryRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  class ProductTableDef(tag:Tag) extends Table[Product](tag,"product"){

    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.SqlType("not null"))
    def count = column[Int]("count")
    def producer = column[String]("producer")
    def subcategory_id = column[Int]("subcategory_id",O.SqlType("not null"))
    def subcategory_fk = foreignKey("subcategory_fk",subcategory_id,
      subcategoryTable)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,name,count,producer,subcategory_id) <>((Product.apply _).tupled, Product.unapply)
  }
  import cR.SubCategoryTableDef
  val products = TableQuery[ProductTableDef]
  val subcategoryTable = TableQuery[SubCategoryTableDef ]
  def list(): Future[Seq[Product]] = db.run{
    products.result
  }
  def getById(id:Int): Future[Option[Product]] = db.run{
    products.filter(_.id === id).result.headOption;
  }
  def create(name:String,count:Int,producer:String,subcategory_id:Int): Future[Product] = db.run{
    (products.map(c=>(c.name,c.count,c.producer,c.subcategory_id))
      returning products.map(_.id)
      into{ case((name,count,producer,subcategory_id),id)=>Product(id,name,count,producer,subcategory_id)}) += (name,count,producer,subcategory_id)
  }
  def delete(productID:Int): Future[Unit] = {
    db.run(products.filter(_.id === productID).delete).map(_=>())
  }
  def update(productID:Int, new_product:Product):Future[Unit] = {
    val updatedProduct = new_product.copy(productID)
    db.run(products.filter(_.id===productID).update(updatedProduct)).map(_=>())
  }
}

