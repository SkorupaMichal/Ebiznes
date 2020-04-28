package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._


@Singleton
class SubCategoryRepository @Inject()(dbConfigProvider:DatabaseConfigProvider,protected val cR:CategoryRepository)(implicit executionContext: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class SubCategoryTableDef (tag:Tag) extends Table[SubCategory](tag,"subcategory"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.Unique)
    def description = column[String]("description")
    def category_id = column[Int]("category_id")
    def category_fk = foreignKey("category_fk",category_id,categories)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,name,description,category_id) <>((SubCategory.apply _).tupled,SubCategory.unapply)
  }
  import cR.CategoryTableDef
  val categories = TableQuery[CategoryTableDef]
  val subcategories = TableQuery[SubCategoryTableDef]

  def list():Future[Seq[SubCategory]] = db.run{
    subcategories.result
  }
  def getById(subcategoryId:Int):Future[Option[SubCategory]] = db.run{
    subcategories.filter(_.id === subcategoryId).result.headOption
  }
  def getByCategoryId(categoryId:Int):Future[Seq[SubCategory]] = db.run{
    subcategories.filter(_.category_id === categoryId).result
  }
  def deleteByCategoryId(categoryId:Int):Future[Unit] = db.run{
    subcategories.filter(_.category_id === categoryId).delete.map(_=>())
  }
  def create(name:String,description:String,category_id:Int):Future[SubCategory] = db.run{
    (subcategories.map(c=>(c.name,c.description,c.category_id))
      returning subcategories.map(_.id)
      into{case((name,description,category_id),id)=>SubCategory(id,name,description,category_id)}
      ) += (name,description,category_id)
  }
  def delete(subcategoryId:Int):Future[Unit] = db.run{
    subcategories.filter(_.id === subcategoryId).delete.map(_=>())
  }
  def update(subcategoryId:Int,new_subcategory:SubCategory):Future[Unit] = {
    val updated_subcategories = new_subcategory.copy(subcategoryId)
    db.run(subcategories.filter(_.id === subcategoryId).update(updated_subcategories)).map(_=>())
  }
}
