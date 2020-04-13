package controllers
import javax.inject._
import play.api.mvc._

@Singleton
class DeliveryController @Inject()(cc:ControllerComponents) extends AbstractController(cc){
  /*Delivery controller*/

  def getDelivery = Action{
    Ok("Delivery" )
  }

  def createDelivery = Action{
    Ok("Create Delivery" )
  }

  def updateDelivery(orderId: Int) = Action{
    Ok("Update Delivery")
  }

  def deleteDelivery(orderId: Int) = Action{
    Ok("Delete Delivery")
  }
}
