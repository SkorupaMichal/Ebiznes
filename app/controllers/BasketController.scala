package controllers

import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import scala.util.{Failure, Success}
import scala.concurrent.{Await, ExecutionContext, Future, duration}

case class CreateBasketForm(description: String, userId: String)

case class UpdateBasketForm(id: Int, description: String, userId: String)

import utils.auth.{DefaultEnv, HasRole}

@Singleton
class BasketController @Inject()(cc: ControllerComponents, dd: MessagesControllerComponents, repo: BasketRepository,
                                 userRepo: daos.UserDAOImpl,
                                 silhouette: Silhouette[DefaultEnv])(implicit ec: ExecutionContext) extends MessagesAbstractController(dd) {
  /*Basket controller*/
  val basketForm: Form[CreateBasketForm] = Form {
    mapping(
      "description" -> nonEmptyText,
      "userId" -> nonEmptyText
    )(CreateBasketForm.apply)(CreateBasketForm.unapply)
  }
  val updateBasketForm: Form[UpdateBasketForm] = Form {
    mapping(
      "id" -> number,
      "description" -> nonEmptyText,
      "userId" -> nonEmptyText
    )(UpdateBasketForm.apply)(UpdateBasketForm.unapply)
  }
  var users: Seq[User] = Seq[User]()

  def getUsersSeq = {
    userRepo.list().onComplete {
      case Success(c) => users = c
      case Failure(_) => print("fail")
    }

  }

  def index = Action { implicit request =>
    getUsersSeq
    Ok(views.html.basketadd(basketForm, users))

  }

  def getAllBaskets: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map { basket =>
      Ok(views.html.baskets(basket))
    }
  }

  def getBasketByID(basketid: Int) = Action.async { implicit request =>
    repo.getById(basketid).map {
      cart =>
        cart match {
          case Some(i) => Ok(Json.toJson(i))
          case None => Ok("Non object")
        }
    }
  }

  def getCurrentBasket = Action {
    Ok("Return Basket");
  }

  def createBasket = Action.async { implicit request =>
    getUsersSeq
    basketForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.basketadd(errorForm, users)))
      },
      basket => {
        repo.create(basket.description, basket.userId).map(_ =>
          Redirect(routes.BasketController.getAllBaskets()).flashing("success" -> "basket.created")
        )
      }
    )
  }

  def updateBasket(id: Int): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    getUsersSeq
    val basket = repo.getById(id)
    basket.map(b => {
      val bForm = updateBasketForm.fill(UpdateBasketForm(b.head.id, b.head.description, b.head.userId))
      Ok(views.html.basketupdate(bForm, users))
    })
  }

  def updateBasketHandle = Action.async { implicit request =>
    getUsersSeq
    updateBasketForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.basketupdate(errorForm, users))
        )
      },
      basket => {
        repo.update(basket.id, Basket(basket.id, basket.description, basket.userId)).map {
          _ => Redirect(routes.BasketController.updateBasket(basket.id)).flashing("success" -> "basket update")
        }
      }
    )
  }

  def addToBasket(productid: Int) = Action {
    Ok("Add product to basket")
  }

  def deleteBasket(basketId: Int): Action[AnyContent] = Action {
    repo.delete(basketId);
    Redirect("/baskets")
  }

  /*Json api*/
  def getAllBasketsJSON: Action[AnyContent] = Action.async { implicit request =>
    val baskets = repo.list()
    Await.result(baskets, duration.Duration.Inf)
    baskets.map(b => Ok(Json.toJson(b)))
  }

  def getBasketByIdJSON(id: Int): Action[AnyContent] = Action.async { implicit request =>
    val baskets = repo.getById(id)
    Await.result(baskets, duration.Duration.Inf)
    baskets.map(b => Ok(Json.toJson(b)))
  }

  def getBasketByUserId(userID: String): Action[AnyContent] = Action.async { implicit request =>
    val userbasket = repo.getByUserId(userID)
    Await.result(userbasket, duration.Duration.Inf)
    userbasket.map(b => Ok(Json.toJson(b)))
  }

  def getReqestJson(request: JsValue): (String, String) = {
    var descrption = ""
    var userID = ""
    (request \ "description").asOpt[String].map { desc =>
      descrption = desc
    }.getOrElse(BadRequest("Oho zly json"))
    (request \ "user_id").asOpt[String].map { usid =>
      userID = usid
    }.getOrElse(BadRequest("Zla skladnia"))

    (descrption, userID)
  }

  def createBasketJson = Action.async { implicit request: Request[AnyContent] =>
    /*Do dopracowania*/
    silhouette.UserAwareRequestHandler { userAwareRequest =>
      Future.successful(HandlerResult(Ok, userAwareRequest.identity))
    }(request).flatMap {
      case HandlerResult(r, Some(user)) => {
        request.body.asJson match {
          case Some(json) => {
            var basket = getReqestJson(json)
            repo.create(basket._1, user.id).map(basket=>Ok(Json.toJson(basket)))
          }
          case None => Future.successful(InternalServerError("Not valid"))
        }
      }
      case HandlerResult(result, None) =>{
        Future.successful(InternalServerError("Not token"))
      }
    }
  }

  def deleteBasketJson(basketId: Int) = Action { request =>
    Await.result(repo.delete(basketId), duration.Duration.Inf)
    Ok("")
  }

  def deleteBasketByUserIdJson(userId: String) = Action { request =>
    Await.result(repo.deleteBasketByUser(userId), duration.Duration.Inf)
    Ok("")
  }

  def updateBasketJson(basketId: Int) = Action(parse.json) { implicit request =>
    val updateBasket = getReqestJson(request.body)
    repo.update(basketId, Basket(basketId, updateBasket._1, updateBasket._2))
    Ok("")
  }
}
