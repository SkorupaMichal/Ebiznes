package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsResult, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

@Singleton
class CategoryRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class CategoryTableDef(tag:Tag) extends Table[Category](tag,"category"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.Unique)
    def description = column[String]("description")
    def * = (id,name,description)<>((Category.apply _).tupled,Category.unapply)
  }

  val categories = TableQuery[CategoryTableDef]
  def list(): Future[Seq[Category]] = db.run{
    categories.result
  }
  def getById(id:Int):Future[Option[Category]] = db.run{
    categories.filter(_.id===id).result.headOption
  }
  def create(name:String,description:String):Future[Category] = db.run{
    (categories.map(c=>(c.name,c.description))
      returning categories.map(_.id)
      into{case((name,description),id)=>Category(id,name,description)})+=(name,description)
  }
  def delete(categoryId: Int):Future[Unit]= db.run{
    categories.filter(_.id===categoryId).delete.map(_=>())
  }
  def update(categoryId:Int,newCategory:Category):Future[Unit] = {
    val updatedCategory = newCategory.copy(categoryId)
    db.run(categories.filter(_.id===categoryId).update(updatedCategory).map(_=>()))
  }

}
