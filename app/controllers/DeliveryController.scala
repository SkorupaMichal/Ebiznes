package controllers
import models.{DeliveryRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class DeliveryController @Inject()(cc:ControllerComponents,deliverRepo:DeliveryRepository)(implicit ex:ExecutionContext) extends AbstractController(cc){
  /*Delivery controller*/

  def getDelivery = Action.async{ implicit request =>
    deliverRepo.list().map(
      delivers => Ok(Json.toJson(delivers))
    )
    //Ok("Delivery" )
  }
  def getDeliverById(deliverId:Int) = Action.async{ implicit request =>
    deliverRepo.getById(deliverId).map(
      deliver => deliver match{
        case Some(i) => Ok(Json.toJson(deliver))
        case None => Ok("Brak rodzaju dowozu o podanym id")
      }
    )

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
