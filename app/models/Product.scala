package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

case class Product(id:Int,name:String,category_id:Int)



class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,protected val cR: CategoryRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  class ProductTableDef(tag:Tag) extends Table[Product](tag,"product"){

    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.SqlType("not null"))
    def category_id = column[Int]("category_id",O.SqlType("not null"))
    def category_fk = foreignKey("category_fk",category_id,
      categoryTable)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,name,category_id) <>((Product.apply _).tupled, Product.unapply)
  }
  import cR.CategoryTableDef
  val products = TableQuery[ProductTableDef]
  val categoryTable = TableQuery[CategoryTableDef ]
  def list(): Future[Seq[Product]] = db.run{
    products.result
  }
  def getById(id:Int): Future[Product] = db.run{
    products.filter(_.id === id).result.head;
  }
  def create(name:String,category_id:Int): Future[Product] = db.run{
    (products.map(c=>(c.name,c.category_id))
      returning products.map(_.id)
      into{ case((name,category_id),id)=>Product(id,name,category_id)}) += (name,category_id)
  }
  def delete(productID:Int): Future[Unit] = {
    db.run(products.filter(_.id === productID).delete).map(_=>())
  }
  def update(productID:Int, new_product:Product):Future[Unit] = {
    val updatedProduct = new_product.copy(productID)
    db.run(products.filter(_.id===productID).update(updatedProduct)).map(_=>())
  }
}

