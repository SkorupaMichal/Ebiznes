package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

@Singleton
class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                  protected val cR: SubCategoryRepository,
                                  protected  val catRepo: CategoryRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  class ProductTableDef(tag:Tag) extends Table[Product](tag,"product"){

    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.SqlType("not null"))
    def cost = column[Int]("cost")
    def count = column[Int]("count")
    def producer = column[String]("producer")
    def categoryId = column[Int]("category_id")
    def categoryFk = foreignKey("category_fk",categoryId,
      subcategoryTable)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def subcategoryId = column[Int]("subcategory_id",O.SqlType("not null"))
    def subcategoryFk = foreignKey("subcategory_fk",subcategoryId,
      subcategoryTable)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,name,cost,count,producer,categoryId,subcategoryId) <>((Product.apply _).tupled, Product.unapply)
  }
  import cR.SubCategoryTableDef
  import catRepo.CategoryTableDef
  val products = TableQuery[ProductTableDef]
  val subcategoryTable = TableQuery[SubCategoryTableDef ]
  val categories = TableQuery[CategoryTableDef]
  def list(): Future[Seq[Product]] = db.run{
    products.result
  }
  def getById(id:Int): Future[Option[Product]] = db.run{
    products.filter(_.id === id).result.headOption;
  }
  def getByCategoryId(catid:Int): Future[Seq[Product]] = db.run{
    products.filter(_.categoryId === catid).result
  }
  def getBySubCategoryId(subcatid:Int): Future[Seq[Product]] = db.run{
    products.filter(_.subcategoryId === subcatid).result
  }
  def create(name:String,cost:Int,count:Int,producer:String,categoryId:Int,subcategoryId:Int): Future[Product] = db.run{
    (products.map(c=>(c.name,c.cost,c.count,c.producer,c.categoryId,c.subcategoryId))
      returning products.map(_.id)
      into{ case((name,cost,count,producer,categoryId,subcategoryId),id)=>Product(id,name,cost,count,producer,categoryId,subcategoryId)}) += (name,cost,count,producer,categoryId,subcategoryId)
  }
  def delete(productID:Int): Future[Unit] = {
    db.run(products.filter(_.id === productID).delete).map(_=>())
  }
  def deleteByCategory(categoryId:Int): Future[Unit] = db.run{
    products.filter(_.categoryId === categoryId).delete.map(_=>())
  }
  def deleteBySubcategory(subcatId:Int):Future[Unit] = db.run{
    products.filter(_.subcategoryId === subcatId).delete.map(_=>())
  }
  def update(productID:Int, newProduct:Product):Future[Unit] = {
    val updatedProduct = newProduct.copy(productID)
    db.run(products.filter(_.id===productID).update(updatedProduct)).map(_=>())
  }
}

