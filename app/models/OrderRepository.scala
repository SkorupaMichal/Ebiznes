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
                                protected val bR:BasketRepository,protected val aR:AddressRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  class OrderTableDef(tag:Tag) extends Table[Order](tag,"orders"){
    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def date = column[String]("date")
    def cost = column[Int]("cost")
    def deliverId = column[Int]("deliver_id")
    def deliverFk = foreignKey("deliver_fk",deliverId,
      delivers)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def userId = column[String]("user_id")
    def paymentId = column[Int]("payment_id")
    def paymentFk = foreignKey("payment_fk",paymentId,
      payments)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def basketId = column[Int]("basket_id")
    def basketFk = foreignKey("basket_fk",basketId,
      baskets)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def addressId = column[Int]("address_id")
    def addressFk = foreignKey("address_fk",addressId,
      addresses)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,date,cost,deliverId,userId,paymentId,basketId,addressId)<>((Order.apply _).tupled,Order.unapply)
  }
  import dR.DeliveryTableDef
  import pR.PaymentTableDef
  import bR.BasketTableDef
  import aR.AddressTableDef
  val orders = TableQuery[OrderTableDef]
  val delivers = TableQuery[DeliveryTableDef]
  val payments = TableQuery[PaymentTableDef]
  val baskets = TableQuery[BasketTableDef]
  val addresses = TableQuery[AddressTableDef]
  def list(): Future[Seq[Order]] = db.run{
    orders.result
  }
  def getById(id:Int):Future[Option[Order]] = db.run{
    orders.filter(_.id===id).result.headOption
  }
  def getByUserId(userId:Int):Future[Seq[Order]] = db.run{
    orders.filter(_.deliverId === userId).result
  }
  def getByUserFullInfo(userId:String):Future[Seq[(Int, String, Int, String, String,String,String)]] = db.run{
    val orderByUser = orders.filter(_.userId === userId)
    val sequence = orderByUser join delivers join payments join addresses on{
      case(((order,deliver),payment),address) =>
        order.deliverId === deliver.id &&
          order.paymentId === payment.id &&
          order.addressId === address.id
    }
    def query = for{
      ((((order,deliver),payment),address)) <- sequence
    }yield (order.id,order.date,order.cost,deliver.name,payment.name,address.city,address.street)
    query.result
  }
  def getByDeliverId(deliverID:Int):Future[Seq[Order]] = db.run{
    orders.filter(_.deliverId === deliverID).result
  }
  def getByUserPayment(userId:String,paymentId:Int): Future[Seq[Order]] = db.run{
    orders.filter( m =>m.userId === userId && m.paymentId === paymentId).result
  }
  def createJoin():Future[Seq[(Int, String, Int, String, String,String,String)]] = db.run{
    /// Info o kurierze platnosci uzytkowniku i cena
    val sequence = orders join delivers join payments join addresses  on{
      case(((order,deliver),payment),address) =>
        order.deliverId === deliver.id &&
        order.paymentId === payment.id &&
        order.addressId === address.id

    };
    def query = for{
      ((((order,deliver),payment),address)) <- sequence
    }yield (order.id,order.date,order.cost,deliver.name,payment.name,address.city,address.street)
    query.result
  }
  def create(date:String,cost:Int,deliverid:Int,userid:String,
             paymentid:Int,basketid:Int,addressId:Int):Future[Order] = db.run{
    (orders.map(c=>(c.date,c.cost,c.deliverId,c.userId,c.paymentId,c.basketId,c.addressId))
      returning orders.map(_.id)
      into{case((date,cost,deliverid,userid,paymentid,basketid,addressId),id)=>Order(id,date,cost,deliverid,userid,paymentid,basketid,addressId)})+=(date,cost,deliverid,userid,paymentid,basketid,addressId)
  }
  def delete(orderId: Int):Future[Unit]= db.run{
    orders.filter(_.id===orderId).delete.map(_=>())
  }
  def update(orderId:Int,newOrder:Order):Future[Unit] = {
    val updatedOrder = newOrder.copy(orderId)
    db.run(orders.filter(_.id===orderId).update(updatedOrder).map(_=>()))
  }
}
