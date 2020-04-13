package controllers
import javax.inject._
import play.api.mvc._

@Singleton
class OrderController @Inject() (cc:ControllerComponents) extends  AbstractController(cc){

  def getOrders = Action{
    Ok("Orders" )
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
