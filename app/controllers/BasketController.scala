package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
case class CreateBasketForm(description:String)
case class UpdateBasketForm(id:Int,description:String)

@Singleton
class BasketController @Inject() (cc:ControllerComponents,dd:MessagesControllerComponents,repo:BasketRepository)(implicit ec:ExecutionContext) extends MessagesAbstractController(dd){
  /*Basket controller*/
  val basketForm: Form[CreateBasketForm] = Form{
    mapping(
      "description"->nonEmptyText
    )(CreateBasketForm.apply)(CreateBasketForm.unapply)
  }
  val updateBasketForm: Form[UpdateBasketForm] = Form{
    mapping(
      "id" -> number,
      "description" -> nonEmptyText
    )(UpdateBasketForm.apply)(UpdateBasketForm.unapply)
  }
  def index = Action { implicit request =>
    Ok(views.html.basketadd(basketForm))

  }
  def get_AllBaskets:Action[AnyContent] = Action.async{implicit request=>
    repo.list().map{basket=>
      Ok(views.html.baskets(basket))
    }
  }
  def getBasketByID(basketid:Int) = Action.async { implicit  request=>
      repo.getById(basketid).map {
        cart => cart match {
          case Some(i) => Ok(Json.toJson(i))
          case None => Ok("Non object")
          }
      }
  }
  def getCurrentBasket = Action{
    Ok("Return Basket");
  }
  def createBasket =  Action.async{ implicit request =>
    basketForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.basketadd(errorForm)))
      },
      basket =>{
        repo.create(basket.description).map(_=>
          Redirect(routes.BasketController.get_AllBaskets()).flashing("success"->"basket.created")
        )
      }
    )
  }
  def updateBasket(id:Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val basket = repo.getById(id)
    basket.map(b=>{
      val bForm = updateBasketForm.fill(UpdateBasketForm(b.head.id,b.head.description))
      Ok(views.html.basketupdate(bForm))
    })
  }
  def updateBasketHandle = Action.async{implicit request=>
    updateBasketForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.basketupdate(errorForm))
        )
      },
      basket =>{
      repo.update(basket.id,Basket(basket.id,basket.description)).map{
        _ => Redirect(routes.BasketController.updateBasket(basket.id)).flashing("success"->"basket update")
      }
  }
    )
  }
  def addToBasket(productid:Int) = Action{
    Ok("Add product to basket")
  }
  def deleteBasket(basketid:Int):Action[AnyContent] = Action{
    repo.delete(basketid);
    Redirect("/baskets")
  }

}
