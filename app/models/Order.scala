package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

case class Order(id:Int,name:String)

@Singleton
class OrderRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class OrderTableDef(tag:Tag) extends Table[Order](tag,"category"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name",O.Default(""))
    def * = (id,name)<>((Order.apply _).tupled,Order.unapply)
  }

  val orders = TableQuery[OrderTableDef]
  def list(): Future[Seq[Order]] = db.run{
    orders.result
  }
  def getById(id:Int):Future[Order] = db.run{
    orders.filter(_.id===id).result.head
  }
  def create(name:String):Future[Order] = db.run{
    (orders.map(c=>(c.name))
      returning orders.map(_.id)
      into{case((name),id)=>Order(id,name)})+=(name)
  }
  def delete(orderId: Int):Future[Unit]= db.run{
    orders.filter(_.id===orderId).delete.map(_=>())
  }
  def update(orderId:Int,new_order:Order):Future[Unit] = {
    val updated_order = new_order.copy(orderId)
    db.run(orders.filter(_.id===orderId).update(updated_order).map(_=>()))
  }
}
