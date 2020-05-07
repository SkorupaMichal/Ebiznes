package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._


@Singleton
class DeliveryRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  class DeliveryTableDef(tag:Tag) extends Table[Delivery](tag,"deliver"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.Unique)
    def cost = column[Int]("cost")
    def description = column[String]("description")
    def * = (id,name,cost,description)<>((Delivery.apply _ ).tupled,Delivery.unapply)
  }
  val delivers = TableQuery[DeliveryTableDef]

  def list():Future[Seq[Delivery]] = db.run{
    delivers.result
  }
  def getById(deliverId:Int):Future[Option[Delivery]] = db.run{
    delivers.filter(_.id ===deliverId).result.headOption
  }
  def create(name:String,cost:Int,description:String):Future[Delivery] = db.run{
    (delivers.map(c=>(c.name,c.cost,c.description))
      returning delivers.map(_.id)
      into {case((name,cost,description),id)=>Delivery(id,name,cost,description)}
      )+=(name,cost,description)
  }
  def delete(deliverid:Int):Future[Unit] = db.run{
    delivers.filter(_.id === deliverid).delete.map(_=>())
  }
  def update(deliverid:Int,newDeliver:Delivery):Future[Unit] = db.run{
    val updatedDeliver = newDeliver.copy(deliverid)
    delivers.filter(_.id === deliverid).update(updatedDeliver).map(_=>())
  }
}
