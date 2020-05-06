package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

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
    var payment:Seq[Payment] = Seq[Payment]()
    var deliver:Seq[Delivery] = Seq[Delivery]()
    var basket:Seq[Basket] = Seq[Basket]()
    var user= userRepo.list()

    paymentRepo.list().onComplete{
      case Success(c) => payment = c
      case Failure(_) =>print("fail")
    }
    deliverRepo.list().onComplete{
      case Success(c) => deliver = c
      case Failure(_) =>print("fail")
    }
    basketRepo.list().onComplete{
      case Success(c) => basket = c
      case Failure(_) =>print("fail")
    }
    user.map(u=>Ok(views.html.orderadd(orderForm,payment,deliver,basket,u)))
  }


  def createOrderHandle = Action.async { implicit request =>
    var payment:Seq[Payment] = Seq[Payment]()
    var deliver:Seq[Delivery] = Seq[Delivery]()
    var basket:Seq[Basket] = Seq[Basket]()
    var user:Seq[User]  = Seq[User]();

    paymentRepo.list().onComplete{
      case Success(c) => payment = c
      case Failure(_) =>print("fail")
    }
    deliverRepo.list().onComplete{
      case Success(c) => deliver = c
      case Failure(_) =>print("fail")
    }
    basketRepo.list().onComplete{
      case Success(c) => basket = c
      case Failure(_) =>print("fail")
    }
    userRepo.list().onComplete{
      case Success(c) => user = c
      case Failure(_) =>print("fail")
    }


    orderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderadd(errorForm,payment,deliver,basket,user))
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
    var payment:Seq[Payment] = Seq[Payment]()
    var deliver:Seq[Delivery] = Seq[Delivery]()
    var basket:Seq[Basket] = Seq[Basket]()
    var user:Seq[User]  = Seq[User]();

    paymentRepo.list().onComplete{
      case Success(c) => payment = c
      case Failure(_) =>print("fail")
    }
    deliverRepo.list().onComplete{
      case Success(c) => deliver = c
      case Failure(_) =>print("fail")
    }
    basketRepo.list().onComplete{
      case Success(c) => basket = c
      case Failure(_) =>print("fail")
    }
    userRepo.list().onComplete{
      case Success(c) => user = c
      case Failure(_) =>print("fail")
    }
    val order = orderRepo.getById(orderId)
    order.map(b=>{
      val bForm = updateOrderForm.fill(UpdateOrderForm(b.head.id,b.head.date,b.head.cost,b.head.deliver_id,
        b.head.user_id,b.head.payment_id,b.head.basket_id))
      Ok(views.html.orderupdate(bForm,payment,deliver,basket,user))

    })
  }

  def updateOrderHandle = Action.async{implicit request=>
    var payment:Seq[Payment] = Seq[Payment]()
    var deliver:Seq[Delivery] = Seq[Delivery]()
    var basket:Seq[Basket] = Seq[Basket]()
    var user:Seq[User]  = Seq[User]();

    paymentRepo.list().onComplete{
      case Success(c) => payment = c
      case Failure(_) =>print("fail")
    }
    deliverRepo.list().onComplete{
      case Success(c) => deliver = c
      case Failure(_) =>print("fail")
    }
    basketRepo.list().onComplete{
      case Success(c) => basket = c
      case Failure(_) =>print("fail")
    }
    userRepo.list().onComplete{
      case Success(c) => user = c
      case Failure(_) =>print("fail")
    }
    updateOrderForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.orderupdate(errorForm,payment,deliver,basket,user))
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
  def getOrderByDeliverIdJson(deliverId:Int) = Action.async{implicit request=>
    val orders = orderRepo.getByDeliverId(deliverId)
    Await.result(orders,duration.Duration.Inf)
    orders.map(b=>Ok(Json.toJson(b)))
  }
  def createOrderJson = Action(parse.json){implicit request=>
    /*,date:String,cost:Int,deliver_id:Int,user_id:Int,payment_id:Int,basket_id:Int*/
    val date = (request.body \ "name").as[String]
    val cost = (request.body \ "cost").as[Int]
    val deliverId = (request.body \ "deliver_id").as[Int]
    val userId = (request.body \ "user_id").as[Int]
    val paymentId = (request.body \ "payment_id").as[Int]
    val basketId = (request.body \ "basket_id").as[Int]
    orderRepo.create(date,cost,deliverId,userId,paymentId,basketId)
    Ok("")
  }
  def updateOrderJson(orderId:Int) = Action(parse.json){implicit request=>
    val date = (request.body \ "name").as[String]
    val cost = (request.body \ "cost").as[Int]
    val deliverId = (request.body \ "deliver_id").as[Int]
    val userId = (request.body \ "user_id").as[Int]
    val paymentId = (request.body \ "payment_id").as[Int]
    val basketId = (request.body \ "basket_id").as[Int]
    orderRepo.update(orderId,Order(orderId,date,cost,deliverId,userId,paymentId,basketId))
    Ok("")
  }
  def deleteOrderJson(orderId:Int) = Action{
    Await.result(orderRepo.delete(orderId),duration.Duration.Inf)
    Ok("")
  }
}
