package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.concurrent.{Await, ExecutionContext, Future, duration}
import scala.util.{Failure, Success}

case class CreatePaymentMethodForm(name:String,description:String)
case class UpdatePaymentMethodForm(id:Int,name:String,description:String)

class PaymentMethodsController @Inject() (cc:ControllerComponents,dd:MessagesControllerComponents,paymentRepo:PaymentRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
  /*Payment methods controller*/
  val paymentMethodForm: Form[CreatePaymentMethodForm] = Form{
    mapping(
      "name" -> nonEmptyText,
      "description" ->nonEmptyText)(CreatePaymentMethodForm.apply)(CreatePaymentMethodForm.unapply)
  }
  val updatepaymentMethodForm: Form[UpdatePaymentMethodForm] = Form{
    mapping(
      "id"  -> number,
      "name" -> nonEmptyText,
      "description" ->nonEmptyText)(UpdatePaymentMethodForm.apply)(UpdatePaymentMethodForm.unapply)
  }
  def getPaymentMethods = Action.async{ implicit request =>
    paymentRepo.list().map(
      payment=> Ok(views.html.paymentmethods(payment))
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

  def createPaymentMethod = Action.async{implicit request =>
    paymentMethodForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.paymentmethodadd(errorForm)))
      },
      payment =>{
        paymentRepo.create(payment.name,payment.description).map(_=>
          Redirect(routes.PaymentMethodsController.getPaymentMethods()).flashing("success"->"basket.created")
        )
      }
    )
  }

  def updatePaymentMethod(paymentMethodId: Int):Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
      val payments = paymentRepo.getById(paymentMethodId)
      payments.map(b=>{
        val bForm = updatepaymentMethodForm.fill(UpdatePaymentMethodForm(b.head.id,b.head.name,b.head.description))
        Ok(views.html.paymentupdate(bForm))
    })
  }
  def updatePaymentMethodHandle = Action.async{implicit request=>
    updatepaymentMethodForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.paymentupdate(errorForm))
        )
      },
      payment =>{
        paymentRepo.update(payment.id,Payment(payment.id,payment.name,payment.description)).map{
          _ => Redirect(routes.CategoryController.updateCategory(payment.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deletePaymentMethod(paymentMethodId: Int) = Action{
    paymentRepo.delete(paymentMethodId)
    Redirect("/paymentmethods")
  }
  /*Json api*/
  def getPaymentMethodsJson =  Action.async { implicit request=>
    val paymentmetho = paymentRepo.list()
    Await.result(paymentmetho,duration.Duration.Inf)
    paymentmetho.map(b=>Ok(Json.toJson(b)))
  }
  def getPaymentMethodsByIdJson(pId:Int) =  Action.async { implicit request=>
    val paymentmetho = paymentRepo.getById(pId)
    Await.result(paymentmetho,duration.Duration.Inf)
    paymentmetho.map(b=>Ok(Json.toJson(b)))
  }
  def createPaymentMethodJson = Action(parse.json){implicit request=>
    val name = (request.body \ "name").as[String]
    val description = (request.body \ "description").as[String]
    paymentRepo.create(name,description)
    Ok("")
  }
  def updatePaymentMethodJson(pmId:Int) = Action(parse.json){implicit request=>
    val name = (request.body \ "name").as[String]
    val description = (request.body \ "description").as[String]
    paymentRepo.update(pmId,Payment(pmId,name,description))
    Ok("")
  }
  def deletePaymentMethodJson(pmId:Int) = Action{
    Await.result(paymentRepo.delete(pmId),duration.Duration.Inf)
    Ok("")
  }
}
