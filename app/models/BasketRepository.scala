package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

@Singleton
class BasketRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                 protected val uR:UserRepository)(implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._
  class BasketTableDef(tag: Tag) extends Table[Basket](tag,"Basket"){

    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def description = column[String]("description",O.Default(""))
    def user_id = column[Int]("user_id")
    def user_fk = foreignKey("user_fk",user_id,
      users)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,description,user_id) <> (( Basket.apply _ ).tupled, Basket.unapply)
  }
  import uR.UserTableDef
  val baskets = TableQuery[BasketTableDef]
  val users   = TableQuery[UserTableDef]
  def list(): Future[Seq[Basket]] = db.run{
    baskets.result
  }
  def getById(id:Int): Future[Option[Basket]] = db.run{
    baskets.filter(_.id ===id).result.headOption
  }
  def getByUserId(userId:Int): Future[Seq[Basket]] = db.run{
    baskets.filter(_.user_id === userId).result
  }
  def create(description:String,user_id:Int):Future[Basket] = db.run{
    (baskets.map(c => (c.description,c.user_id))
      returning baskets.map(_.id)
      into {case ((description,user_id),id)=>Basket(id,description,user_id)}) +=(description,user_id)
  }
  def delete(basketID:Int):Future[Unit] = {
    db.run(baskets.filter(_.id === basketID).delete).map(_=>())
  }
  def update(bid:Int,new_basket: Basket):Future[Unit]={
    val updatedBasket: Basket = new_basket.copy(bid)
    db.run(baskets.filter(_.id === bid).update(updatedBasket)).map(_=>())
  }

}