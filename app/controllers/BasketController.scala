package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class BasketController @Inject() (cc:ControllerComponents) extends AbstractController(cc){

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
