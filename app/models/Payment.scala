package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

case class Payment(id:Int,name:String,description:String)

@Singleton
class PaymentRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class PaymentTableDef(tag:Tag) extends Table[Payment](tag,"category"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.Unique)
    def description = column[String]("description")
    def * = (id,name,description)<>((Payment.apply _).tupled,Payment.unapply)
  }

  val payments = TableQuery[PaymentTableDef]
  def list(): Future[Seq[Payment]] = db.run{
    payments.result
  }
  def getById(id:Int):Future[Payment] = db.run{
    payments.filter(_.id===id).result.head
  }
  def create(name:String,description:String):Future[Payment] = db.run{
    (payments.map(c=>(c.name,c.description))
      returning payments.map(_.id)
      into{case((name,description),id)=>Payment(id,name,description)})+=(name,description)
  }
  def delete(categoryId: Int):Future[Unit]= db.run{
    payments.filter(_.id===categoryId).delete.map(_=>())
  }
  def update(categoryId:Int,new_payment:Payment):Future[Unit] = {
    val updated_payment = new_payment.copy(categoryId)
    db.run(payments.filter(_.id===categoryId).update(updated_payment).map(_=>()))
  }
}