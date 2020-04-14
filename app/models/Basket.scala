package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

case class Basket(id:Int,description:String)
@Singleton
class BasketRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._
  class BasketTableDef(tag: Tag) extends Table[Basket](tag,"basket"){

    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def description = column[String]("description",O.Default(""))
    def * = (id,description) <> (( Basket.apply _ ).tupled, Basket.unapply)
  }
  val baskets = TableQuery[BasketTableDef]

  def list(): Future[Seq[Basket]] = db.run{
    baskets.result
  }
  def getById(id:Int): Future[Basket] = db.run{
    baskets.filter(_.id ===id).result.head
  }
  def create(description:String):Future[Basket] = db.run{
    (baskets.map(c => (c.description))
    returning baskets.map(_.id)
    into {case ((description),id)=>Basket(id,description)}) +=(description)
  }
  def delete(basketID:Int):Future[Unit] = {
    db.run(baskets.filter(_.id === basketID).delete).map(_=>())
  }
  def update(bid:Int,new_basket: Basket):Future[Unit]={
    val updatedBasket: Basket = new_basket.copy(bid)
    db.run(baskets.filter(_.id === bid).update(updatedBasket)).map(_=>())
  }

}