package controllers
import models.{OrderRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class OrderController @Inject() (cc:ControllerComponents,orderRepo:OrderRepository)(implicit ex:ExecutionContext) extends  AbstractController(cc){
  /*Order controller*/

  def getOrders = Action.async{ implicit  request =>
    orderRepo.list().map(
      order => Ok(Json.toJson(order))
    )
    //Ok("Orders" )
  }
  def getOrderByID(orderID:Int) = Action.async{implicit  request=>
    orderRepo.getById(orderID).map(
      order => order match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak zamowien o podanym id")
      }
    )
  }
  def createOrder = Action{
    Ok("Create Order" )
  }

  def updateOrder(orderId: Int) = Action{
    Ok("Update Order")
  }

  def deleteOrder(orderId: Int) = Action{
    Ok("Delete Order")
  }
}
