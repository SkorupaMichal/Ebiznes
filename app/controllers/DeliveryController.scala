package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.concurrent.{Await, ExecutionContext, Future, duration}
import scala.util.{Failure, Success}

case class CreateDeliverForm(name:String,cost:Int,description:String)
case class UpdateDeliverForm(id:Int,name:String,cost:Int,description:String)

@Singleton
class DeliveryController @Inject()(cc:ControllerComponents,dd:MessagesControllerComponents,deliverRepo:DeliveryRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
  /*Delivery controller*/
  val deliverForm: Form[CreateDeliverForm] = Form{
    mapping(
      "name" -> nonEmptyText,
      "cost" -> number,
      "description" ->nonEmptyText)(CreateDeliverForm.apply)(CreateDeliverForm.unapply)
  }
  val updateDeliverForm: Form[UpdateDeliverForm] = Form{
    mapping(
      "id"  -> number,
      "name" -> nonEmptyText,
      "cost" -> number,
      "description" ->nonEmptyText)(UpdateDeliverForm.apply)(UpdateDeliverForm.unapply)
  }

  def getDelivery = Action.async{ implicit request =>
    deliverRepo.list().map(
      delivers => Ok(views.html.delivers(delivers))
    )
  }
  def getDeliverById(deliverId:Int) = Action.async{ implicit request =>
    deliverRepo.getById(deliverId).map(
      deliver => deliver match{
        case Some(i) => Ok(views.html.delivers(Seq[Delivery](i)))
        case None => Ok("Brak rodzaju dowozu o podanym id")
      }
    )

  }
  def createDelivery =Action.async{implicit request =>
    deliverForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.deliveradd(errorForm)))
      },
      deliver =>{
        deliverRepo.create(deliver.name,deliver.cost,deliver.description).map(_=>
          Redirect(routes.DeliveryController.getDelivery()).flashing("success"->"basket.created")
        )
      }
    )
  }

  def updateDelivery(deliverId: Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val deliver = deliverRepo.getById(deliverId)
    deliver.map(b=>{
      val bForm = updateDeliverForm.fill(UpdateDeliverForm(b.head.id,b.head.name,b.head.cost,b.head.description))
      Ok(views.html.deliverupdate(bForm))
    })
  }
  def updateDeliverHandle = Action.async{implicit request=>
    updateDeliverForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.deliverupdate(errorForm))
        )
      },
      deliver =>{
        deliverRepo.update(deliver.id,Delivery(deliver.id,deliver.name,deliver.cost,deliver.description)).map{
          _ => Redirect(routes.DeliveryController.updateDelivery(deliver.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deleteDelivery(userId: Int) = Action{
    deliverRepo.delete(userId)
    Redirect("/delivers")
  }

  /*Json api*/
  def getDeliversJson = Action.async{implicit request=>
    val delivers = deliverRepo.list()
    Await.result(delivers,duration.Duration.Inf)
    delivers.map(b=>Ok(Json.toJson(b)))
  }
  def getDeliverByIdJson(deliverID:Int) = Action.async{implicit request=>
    val delivers = deliverRepo.getById(deliverID)
    Await.result(delivers,duration.Duration.Inf)
    delivers.map(b=>Ok(Json.toJson(b)))
  }
  def createDeliverJson = Action(parse.json){implicit request=>
    val name = (request.body \ "name").as[String]
    val cost = (request.body \ "cost").as[Int]
    val description = (request.body \ "description").as[String]
    deliverRepo.create(name,cost,description)
    Ok("")
  }
  def updateDeliverJson(deliverId:Int) = Action(parse.json){implicit request=>
    val name = (request.body \ "name").as[String]
    val cost = (request.body \ "cost").as[Int]
    val description = (request.body \ "description").as[String]
    deliverRepo.update(deliverId,Delivery(deliverId,name,cost,description))
    Ok("")
  }
  def deleteDeliverJson(deliverId:Int) = Action{
    Await.result(deliverRepo.delete(deliverId),duration.Duration.Inf)
    Ok("")
  }
}
