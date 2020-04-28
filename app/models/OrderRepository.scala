package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

@Singleton
class OrderRepository @Inject()(dbConfigProvider:DatabaseConfigProvider,protected val uR: UserRepository,
                                protected  val dR: DeliveryRepository,protected val pR:PaymentRepository,
                                protected val bR:BasketRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class OrderTableDef(tag:Tag) extends Table[Order](tag,"orders"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def date = column[String]("date")
    def cost = column[Int]("cost")
    def deliver_id = column[Int]("deliver_id")
    def deliver_fk = foreignKey("deliver_fk",deliver_id,
      delivers)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def user_id = column[Int]("user_id")
    def user_fk = foreignKey("user_fk",user_id,
      users)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def payment_id = column[Int]("payment_id")
    def payment_fk = foreignKey("payment_fk",payment_id,
      payments)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def basket_id = column[Int]("basket_id")
    def basket_fk = foreignKey("basket_fk",basket_id,
      baskets)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,date,cost,deliver_id,user_id,payment_id,basket_id)<>((Order.apply _).tupled,Order.unapply)
  }
  import uR.UserTableDef
  import dR.DeliveryTableDef
  import pR.PaymentTableDef
  import bR.BasketTableDef
  val orders = TableQuery[OrderTableDef]
  val users  = TableQuery[UserTableDef]
  val delivers = TableQuery[DeliveryTableDef]
  val payments = TableQuery[PaymentTableDef]
  val baskets = TableQuery[BasketTableDef]
  def list(): Future[Seq[Order]] = db.run{
    orders.result
  }
  def getById(id:Int):Future[Option[Order]] = db.run{
    orders.filter(_.id===id).result.headOption
  }
  def getByUserId(userId:Int):Future[Seq[Order]] = db.run{
    orders.filter(_.user_id === userId).result
  }
  def getByDeliverId(deliverID:Int):Future[Seq[Order]] = db.run{
    orders.filter(_.deliver_id === deliverID).result
  }
  def createJoin():Future[Seq[(Int,String,Int,String,String,String)]] = db.run{
    /// Info o kurierze platnosci uzytkowniku i cena
    val sequence = orders join delivers join payments join users on{
      case(((order,deliver),payment),user) =>
        order.deliver_id === deliver.id &&
        order.payment_id === payment.id &&
        order.user_id === user.id
    };
    def query = for{
      (((order,deliver),payment),user) <- sequence
    }yield (order.id,order.date,order.cost,deliver.name,payment.name,user.login)
    query.result
  }
  def create(date:String,cost:Int,deliver_id:Int,user_id:Int,
             payment_id:Int,basket_id:Int):Future[Order] = db.run{
    (orders.map(c=>(c.date,c.cost,c.deliver_id,c.user_id,c.payment_id,c.basket_id))
      returning orders.map(_.id)
      into{case((date,cost,deliver_id,user_id,payment_id,basket_id),id)=>Order(id,date,cost,deliver_id,user_id,payment_id,basket_id)})+=(date,cost,deliver_id,user_id,payment_id,basket_id)
  }
  def delete(orderId: Int):Future[Unit]= db.run{
    orders.filter(_.id===orderId).delete.map(_=>())
  }
  def update(orderId:Int,new_order:Order):Future[Unit] = {
    val updated_order = new_order.copy(orderId)
    db.run(orders.filter(_.id===orderId).update(updated_order).map(_=>()))
  }
}
