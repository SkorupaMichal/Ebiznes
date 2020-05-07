package models
import play.api.libs.json._
case class Basket(id:Int,description:String,userId:Int)
object Basket{
  implicit val basketForm = Json.format[Basket]
}
