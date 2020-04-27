package models
import play.api.libs.json.Json

case class ProductBasket(id:Int,basket_id:Int,product_id:Int)
object ProductBasket{
  implicit val productBasketForm = Json.format[ProductBasket]
}
