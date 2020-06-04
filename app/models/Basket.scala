package models
import play.api.libs.json._
case class Basket(id:Int,description:String,userId:String)
object Basket{
  implicit val basketForm = Json.format[Basket]
}
