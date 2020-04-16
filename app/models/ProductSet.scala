package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

case class ProductSet(id:Int,name:String,description:String)
object ProductSet{
  implicit val productSetForm = Json.format[ProductSet]
}
@Singleton
class ProductSetRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class ProductSetTableDef(tag:Tag) extends Table[ProductSet](tag,"productSet"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.Unique)
    def description = column[String]("description")
    def * = (id,name,description)<>((ProductSet.apply _).tupled,ProductSet.unapply)
  }

  val productsets = TableQuery[ProductSetTableDef]
  def list(): Future[Seq[ProductSet]] = db.run{
    productsets.result
  }
  def getById(id:Int):Future[Option[ProductSet]] = db.run{
    productsets.filter(_.id===id).result.headOption
  }
  def create(name:String,description:String):Future[ProductSet] = db.run{
    (productsets.map(c=>(c.name,c.description))
      returning productsets.map(_.id)
      into{case((name,description),id)=>ProductSet(id,name,description)})+=(name,description)
  }
  def delete(productSetId: Int):Future[Unit]= db.run{
    productsets.filter(_.id===productSetId).delete.map(_=>())
  }
  def update(productSetId:Int,new_category:ProductSet):Future[Unit] = {
    val updated_category = new_category.copy(productSetId)
    db.run(productsets.filter(_.id===productSetId).update(updated_category).map(_=>()))
  }
}
