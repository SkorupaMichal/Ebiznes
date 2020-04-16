package controllers
import models.{PaymentRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

class PaymentMethodsController @Inject() (cc:ControllerComponents, paymentRepo:PaymentRepository)(implicit ex:ExecutionContext) extends AbstractController(cc){
  /*Payment methods controller*/

  def getPaymentMethods = Action.async{ implicit request =>
    paymentRepo.list().map(
      payment=> Ok(Json.toJson(payment))
    )
    //Ok("PaymentMethods" )
  }
  def getPaymentMethodByID(paymentId:Int) = Action.async{ implicit request =>
    paymentRepo.getById(paymentId).map(
      payment=>payment match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Nie znaleziono metody platnosci o podanym id")
      }
    )
  }

  def createPaymentMethod = Action{
    Ok("Create PaymentMethods" )
  }

  def updatePaymentMethod(paymentMethodId: Int) = Action{
    Ok("Update PaymentMethods")
  }

  def deletePaymentMethod(paymentMethodId: Int) = Action{
    Ok("Delete PaymentMethods")
  }

}
