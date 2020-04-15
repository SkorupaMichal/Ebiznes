package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

case class CreateBasketForm(description:String)
@Singleton
class BasketController @Inject() (cc:ControllerComponents,repo:BasketRepository)(implicit ec:ExecutionContext) extends AbstractController(cc){
  /*Basket controller*/
  val basketForm: Form[CreateBasketForm] = Form{
    mapping(
      "description"->nonEmptyText
    )(CreateBasketForm.apply)(CreateBasketForm.unapply)
  }

  def get_AllBaskets = Action.async{implicit request=>
    repo.list().map{basket=>
      Ok(Json.toJson(basket))
    }
  }
  def getBasketByID(basketid:Int) = Action.async { implicit  request=>
      repo.getById(basketid).map{
        cart=>Ok(Json.toJson(cart))
      }
  }
  def getCurrentBasket = Action{
    Ok("Return Basket");
  }
  def createBasket =  Action{
    Ok("Create basket")
  }
  def addToBasket(productid:Int) = Action{
    Ok("Add product to basket")
  }
  def deleteBasket(basketid:Int) = Action{
    Ok("Delete basket")
  }

}
