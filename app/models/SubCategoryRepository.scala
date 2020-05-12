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
    def categoryId = column[Int]("category_id")
    def categoryFk = foreignKey("category_fk",categoryId,categories)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,name,description,categoryId) <>((SubCategory.apply _).tupled,SubCategory.unapply)
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
    subcategories.filter(_.categoryId === categoryId).result
  }
  def deleteByCategoryId(categoryId:Int):Future[Unit] = db.run{
    subcategories.filter(_.categoryId === categoryId).delete.map(_=>())
  }
  def create(name:String,description:String,categoryId:Int):Future[SubCategory] = db.run{
    (subcategories.map(c=>(c.name,c.description,c.categoryId))
      returning subcategories.map(_.id)
      into{case((name,description,categoryId),id)=>SubCategory(id,name,description,categoryId)}
      ) += (name,description,categoryId)
  }
  def delete(subcategoryId:Int):Future[Unit] = db.run{
    subcategories.filter(_.id === subcategoryId).delete.map(_=>())
  }
  def update(subcategoryId:Int,newSubcategory:SubCategory):Future[Unit] = {
    val updatedSubcategories = newSubcategory.copy(subcategoryId)
    db.run(subcategories.filter(_.id === subcategoryId).update(updatedSubcategories)).map(_=>())
  }
}
