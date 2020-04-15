package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

case class Delivery(id:Int,name:String,description: String)
object Delivery{
  implicit val deliveryForm = Json.format[Delivery]
}
@Singleton
class DeliveryRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  class DeliveryTableDef(tag:Tag) extends Table[Delivery](tag,"delivery"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("id",O.Unique)
    def description = column[String]("id")
    def * = (id,name,description)<>((Delivery.apply _ ).tupled,Delivery.unapply)
  }
  val delivers = TableQuery[DeliveryTableDef]

  def list():Future[Seq[Delivery]] = db.run{
    delivers.result
  }
  def getById(deliverId:Int):Future[Delivery] = db.run{
    delivers.filter(_.id ===deliverId).result.head
  }
  def create(name:String,description:String):Future[Delivery] = db.run{
    (delivers.map(c=>(c.name,c.description))
      returning delivers.map(_.id)
      into {case((name,description),id)=>Delivery(id,name,description)}
      )+=(name,description)
  }
  def delete(deliverid:Int):Future[Unit] = db.run{
    delivers.filter(_.id === deliverid).delete.map(_=>())
  }
  def update(deliverid:Int,new_deliver:Delivery):Future[Unit] = db.run{
    val updated_deliver = new_deliver.copy(deliverid)
    delivers.filter(_.id === deliverid).update(updated_deliver).map(_=>())
  }
}
