package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{Await, ExecutionContext, Future, duration}
import slick.jdbc.H2Profile.api._

import scala.util.{Failure, Success}

case class CreateOrderForm(date:String,cost:Int,deliverId:Int,userId:Int,paymentId:Int,basketId:Int)
case class UpdateOrderForm(id:Int,date:String,cost:Int,deliverId:Int,userId:Int,paymentId:Int,basketId:Int)

@Singleton
class OrderController @Inject() (cc:ControllerComponents,orderRepo:OrderRepository,dd:MessagesControllerComponents,userRepo:UserRepository,
                                 deliverRepo:DeliveryRepository,paymentRepo:PaymentRepository,
                                 basketRepo:BasketRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
  /*Order controller*/
  val orderForm: Form[CreateOrderForm] = Form{
    mapping(
      "date" -> nonEmptyText,
      "cost" -> number,
      "deliverId" -> number,
      "userId" ->number,
      "paymentId" -> number,
      "basketId" -> number
    )(CreateOrderForm.apply)(CreateOrderForm.unapply)
  }
  val updateOrderForm: Form[UpdateOrderForm] = Form{
    mapping(
      "id"  -> number,
      "date" -> nonEmptyText,
      "cost" -> number,
      "deliverId" -> number,
      "userId" ->number,
      "paymentId" -> number,
      "basketId" -> number
    )(UpdateOrderForm.apply)(UpdateOrderForm.unapply)
  }
  var payments:Seq[Payment] = Seq[Payment]()
  var delivers:Seq[Delivery] = Seq[Delivery]()
  var baskets:Seq[Basket] = Seq[Basket]()
  var users:Seq[User] = Seq[User]()
  def getPaymentsSeq = {
    paymentRepo.list().onComplete{
      case Success(c) => payments = c
      case Failure(_) =>print("fail")
    }
  }
  def getDeliversSeq = {
    deliverRepo.list().onComplete{
      case Success(c) => delivers = c
      case Failure(_) =>print("fail")
    }
  }
  def getBasketsSeq = {
    basketRepo.list().onComplete{
      case Success(c) => baskets = c
      case Failure(_) =>print("fail")
    }
  }
  def getUserSeq = {
    userRepo.list().onComplete{
      case Success(c) => users = c
      case Failure(_) =>print("fail")
    }
  }

  def getOrders = Action.async{ implicit  request =>
    val q =  orderRepo.createJoin()
    q.map(c=>Ok(views.html.orders(c)))
  }
  def getOrderByID(orderID:Int) = Action.async{implicit  request=>
    orderRepo.getById(orderID).map(
      order => order match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak zamowien o podanym id")
      }
    )
  }
  def createOrder:Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    getPaymentsSeq
    getBasketsSeq
    getDeliversSeq
    var user= userRepo.list()
    user.map(u=>Ok(views.html.orderadd(orderForm,payments,delivers,baskets,u)))
  }


  def createOrderHandle = Action.async { implicit request =>
    getPaymentsSeq
    getBasketsSeq
    getDeliversSeq
    getUserSeq
    orderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderadd(errorForm,payments,delivers,baskets,users))
        )
      },
      order => {
        orderRepo.create(order.date, order.cost, order.deliverId,
          order.userId,order.paymentId,order.basketId).map { _ =>
          Redirect(routes.OrderController.getOrders()).flashing("success" -> "product.created")
        }
      }
    )
  }
  def updateOrder(orderId: Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    getPaymentsSeq
    getBasketsSeq
    getDeliversSeq
    getUserSeq
    val order = orderRepo.getById(orderId)
    order.map(b=>{
      val bForm = updateOrderForm.fill(UpdateOrderForm(b.head.id,b.head.date,b.head.cost,b.head.deliverId,
        b.head.userId,b.head.paymentId,b.head.basketId))
      Ok(views.html.orderupdate(bForm,payments,delivers,baskets,users))

    })
  }

  def updateOrderHandle = Action.async{implicit request=>
    getPaymentsSeq
    getBasketsSeq
    getDeliversSeq
    getUserSeq
    updateOrderForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.orderupdate(errorForm,payments,delivers,baskets,users))
        )
      },
      order =>{
        orderRepo.update(order.id,Order(order.id,order.date,
          order.cost,order.deliverId,order.userId,order.paymentId,order.basketId)).map{
          _ => Redirect(routes.OrderController.updateOrder(order.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deleteOrder(orderId: Int) = Action{
    orderRepo.delete(orderId)
    Redirect("/orders")
  }

  /*Json Api*/
  def getOrderJson = Action.async{ implicit  request =>
    val orders = orderRepo.createJoin()
    Await.result(orders,duration.Duration.Inf)
    orders.map(b=>Ok(Json.toJson(b)))
  }
  def getOrderByIdJson(orderId:Int) = Action.async{implicit request =>
    val orders = orderRepo.getById(orderId)
    Await.result(orders,duration.Duration.Inf)
    orders.map(b=>Ok(Json.toJson(b)))
  }
  def getOrderByUserIdJson(userId:Int) = Action.async{implicit request=>
    val orders = orderRepo.getByUserId(userId)
    Await.result(orders,duration.Duration.Inf)
    orders.map(b=>Ok(Json.toJson(b)))
  }
  def getOrderByUseraPaymentJson(userId:Int,paymentId:Int) = Action.async{implicit request=>
    val orderbyuserpayment = orderRepo.getByUserPayment(userId,paymentId)
    Await.result(orderbyuserpayment,duration.Duration.Inf)
    orderbyuserpayment.map(b=>Ok(Json.toJson(b)))
  }
  def getOrderByDeliverIdJson(deliverId:Int) = Action.async{implicit request=>
    val orders = orderRepo.getByDeliverId(deliverId)
    Await.result(orders,duration.Duration.Inf)
    orders.map(b=>Ok(Json.toJson(b)))
  }
  def getOrderFromReqest(request:MessagesRequest[JsValue]):(String,Int,Int,Int,Int,Int) = {
    var date = ""
    var cost = -1
    var deliverId = -1
    var userId = -1
    var paymentId = -1
    var basketId = -1
    (request.body \ "date").asOpt[String].map{ur=>
      date = ur
    }.getOrElse(BadRequest("Blad"))
    (request.body \ "cost").asOpt[Int].map{cos=>
      cost = cos
    }.getOrElse(BadRequest("Blad"))
    (request.body \ "deliverId").asOpt[Int].map{del=>
      deliverId = del
    }.getOrElse(BadRequest("Blad"))
    (request.body \ "userId").asOpt[Int].map{user=>
      userId = user
    }.getOrElse(BadRequest("Blad"))
    (request.body \ "paymentId").asOpt[Int].map{pay=>
      paymentId = pay
    }.getOrElse(BadRequest("Blad"))
    (request.body \ "basketId").asOpt[Int].map{basket=>
      basketId = basket
    }.getOrElse(BadRequest("Blad"))
    (date,cost,deliverId,userId,paymentId,basketId)
  }
  def createOrderJson = Action(parse.json){implicit request=>
    /*,date:String,cost:Int,deliver_id:Int,user_id:Int,payment_id:Int,basket_id:Int*/
    val order = getOrderFromReqest(request)
    orderRepo.create(order._1,order._2,order._3,order._4,order._5,order._6)
    Ok("")
  }
  def updateOrderJson(orderId:Int) = Action(parse.json){implicit request=>
    val order = getOrderFromReqest(request)
    orderRepo.update(orderId,Order(orderId,order._1,order._2,order._3,order._4,order._5,order._6))
    Ok("")
  }
  def deleteOrderJson(orderId:Int) = Action{
    Await.result(orderRepo.delete(orderId),duration.Duration.Inf)
    Ok("")
  }
}
