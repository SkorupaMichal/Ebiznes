package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.util.{Failure, Success}
import scala.concurrent.{Await, ExecutionContext, Future, duration}

case class CreateBasketForm(description:String,userId:Int)
case class UpdateBasketForm(id:Int,description:String,userId:Int)

@Singleton
class BasketController @Inject() (cc:ControllerComponents,dd:MessagesControllerComponents,repo:BasketRepository,
                                 userRepo:UserRepository)(implicit ec:ExecutionContext) extends MessagesAbstractController(dd){
  /*Basket controller*/
  val basketForm: Form[CreateBasketForm] = Form{
    mapping(
      "description"->nonEmptyText,
      "userId" -> number
    )(CreateBasketForm.apply)(CreateBasketForm.unapply)
  }
  val updateBasketForm: Form[UpdateBasketForm] = Form{
    mapping(
      "id" -> number,
      "description" -> nonEmptyText,
      "userId" -> number
    )(UpdateBasketForm.apply)(UpdateBasketForm.unapply)
  }
  var users:Seq[User] = Seq[User]()

  def getUsersSeq = {
    userRepo.list().onComplete{
      case Success(c) => users = c
      case Failure(_) => print("fail")
    }

  }
  def index = Action { implicit request =>
    getUsersSeq
    Ok(views.html.basketadd(basketForm,users))

  }
  def getAllBaskets:Action[AnyContent] = Action.async{implicit request=>
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
    getUsersSeq
    basketForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.basketadd(errorForm,users)))
      },
      basket =>{
        repo.create(basket.description,basket.userId).map(_=>
          Redirect(routes.BasketController.getAllBaskets()).flashing("success"->"basket.created")
        )
      }
    )
  }
  def updateBasket(id:Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    getUsersSeq
    val basket = repo.getById(id)
    basket.map(b=>{
      val bForm = updateBasketForm.fill(UpdateBasketForm(b.head.id,b.head.description,b.head.user_id))
      Ok(views.html.basketupdate(bForm,users))
    })
  }
  def updateBasketHandle = Action.async{implicit request=>
    getUsersSeq
    updateBasketForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.basketupdate(errorForm,users))
        )
      },
      basket =>{
      repo.update(basket.id,Basket(basket.id,basket.description,basket.userId)).map{
        _ => Redirect(routes.BasketController.updateBasket(basket.id)).flashing("success"->"basket update")
      }
  }
    )
  }
  def addToBasket(productid:Int) = Action{
    Ok("Add product to basket")
  }
  def deleteBasket(basketId:Int):Action[AnyContent] = Action{
    repo.delete(basketId);
    Redirect("/baskets")
  }

  /*Json api*/
  def getAllBasketsJSON: Action[AnyContent] = Action.async{ implicit request =>
    val baskets = repo.list()
    Await.result(baskets,duration.Duration.Inf)
    baskets.map(b=>Ok(Json.toJson(b)))
  }
  def getBasketByIdJSON(id:Int): Action[AnyContent] = Action.async{ implicit request =>
    val baskets = repo.getById(id)
    Await.result(baskets,duration.Duration.Inf)
    baskets.map(b=>Ok(Json.toJson(b)))
  }
  def getBasketByUserId(userID:Int): Action[AnyContent] = Action.async{ implicit request =>
    val userbasket = repo.getByUserId(userID)
    Await.result(userbasket,duration.Duration.Inf)
    userbasket.map(b=>Ok(Json.toJson(b)))
  }
  def createBasketJson = Action(parse.json) { request =>
    /*Do dopracowania*/
    val desc = (request.body \ "description").as[String]
    val pass = (request.body \ "user_id").as[Int]
    repo.create(desc,pass)
    Ok
  }
  def deleteBasketJson(basketId:Int) = Action { request =>
    Await.result(repo.delete(basketId),duration.Duration.Inf)
    Ok("")
  }
  def updateBasketJson(basketId:Int) = Action(parse.json) {implicit request=>
    val decription = (request.body \ "description").as[String]
    val userId = (request.body \ "user_id").as[Int]
    repo.update(basketId,Basket(basketId,decription,userId))
    Ok("")
  }

}
