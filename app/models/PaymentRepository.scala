package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._


@Singleton
class PaymentRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class PaymentTableDef(tag:Tag) extends Table[Payment](tag,"payment"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.Unique)
    def description = column[String]("description")
    def * = (id,name,description)<>((Payment.apply _).tupled,Payment.unapply)
  }

  val payments = TableQuery[PaymentTableDef]
  def list(): Future[Seq[Payment]] = db.run{
    payments.result
  }
  def getById(id:Int):Future[Option[Payment]] = db.run{
    payments.filter(_.id===id).result.headOption
  }
  def create(name:String,description:String):Future[Payment] = db.run{
    (payments.map(c=>(c.name,c.description))
      returning payments.map(_.id)
      into{case((name,description),id)=>Payment(id,name,description)})+=(name,description)
  }
  def delete(categoryId: Int):Future[Unit]= db.run{
    payments.filter(_.id===categoryId).delete.map(_=>())
  }
  def update(categoryId:Int,newPayment:Payment):Future[Unit] = {
    val updatedPayment = newPayment.copy(categoryId)
    db.run(payments.filter(_.id===categoryId).update(updatedPayment).map(_=>()))
  }
}