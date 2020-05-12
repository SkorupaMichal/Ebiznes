package models
import play.api.libs.json.Json

case class ProductBasket(id:Int,basketId:Int,productId:Int)
object ProductBasket{
  implicit val productBasketForm = Json.format[ProductBasket]
}
