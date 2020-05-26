package controllers
import models._
import javax.inject._
import play.api.mvc.{ControllerComponents, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
@Singleton
class ProductBasketController @Inject()(cc:ControllerComponents,productBasketRepo:ProductBasketRepository,dd:MessagesControllerComponents)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){

  var prodbasket:Seq[ProductBasket] = Seq[ProductBasket]()

  def getProdctsByBasketId(basketId:Int) = Action.async{implicit request=>
    val result = productBasketRepo.getFullListOfProductsByBasketId(basketId)
    Await.result(result,Duration.Inf)
    result.map(prod=>Ok(Json.toJson(prod)))
  }
  def getBasketAndProducts(request:MessagesRequest[JsValue]) = {
    var basketId = -1
    var arrayOfProductsId = Array[Int]()
    (request.body \ "basket_id").asOpt[Int].map{ id=>
      basketId = id
    }.getOrElse(BadRequest("Blad"))
    (request.body \ "arrayofid").asOpt[Array[Int]].map{arr=>
      arrayOfProductsId = arr
    }
    (basketId,arrayOfProductsId)
  }
  def addProductToBasket = Action(parse.json){ implicit request =>
    val baskproducts = getBasketAndProducts(request)
    for(i <- baskproducts._2){
      Await.result(productBasketRepo.create(baskproducts._1,i),Duration.Inf)
    }
    Ok("")
  }
}
