package controllers
import javax.inject._
import play.api.mvc._

class PaymentMethodsController @Inject() (cc:ControllerComponents) extends AbstractController(cc){

  def getPaymentMethods = Action{
    Ok("PaymentMethods" )
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
