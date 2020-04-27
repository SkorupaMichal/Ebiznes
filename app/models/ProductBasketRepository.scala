package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

@Singleton
class ProductBasketRepository @Inject() (dbConfigProvider:DatabaseConfigProvider,protected val productRepo:ProductRepository,
                                         protected  val basketRepo: BasketRepository)(implicit executionContext: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile._
  class ProductBasketTableDef(tag: Tag)extends Table[ProductBasket](tag,"productbasket"){

    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def basket_id = column[Int]("basket_id")
    def basket_fk = foreignKey("basket_fk",basket_id,baskets)(_.id,onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    def product_id = column[Int]("product_id")
    def product_fk = foreignKey("product_fk",product_id,products)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,basket_id,product_id)<>((ProductBasket.apply _).tupled,ProductBasket.unapply)
  }

  import basketRepo.BasketTableDef
  import productRepo.ProductTableDef

  val productbasket = TableQuery[ProductBasketTableDef]
  val baskets = TableQuery[BasketTableDef]
  val products = TableQuery[ProductTableDef]

  def list(): Future[Seq[ProductBasket]] = db.run{
    productbasket.result
  }

  def getById(productbasketId:Int): Future[Option[ProductBasket]] = db.run{
    productbasket.filter(_.id === productbasketId).result.headOption
  }
  def getFullListOfProductsByUser(userID:Int) = db.run{
    val bb = baskets.filter(_.user_id === userID)
    val sequence = productbasket join products join baskets on {
      case((cbp,products),bb) =>
        cbp.product_id === products.id &&
        cbp.basket_id === bb.id
    }
    def query = for{
      ((productxbasket,products),bb) <- sequence
    } yield(productxbasket.basket_id,productxbasket.product_id,products.name,products.cost)
    query.result
  }
  def create(basket_id:Int,product_id:Int):Future[ProductBasket] = db.run{
    (productbasket.map(c=>(c.basket_id,c.product_id))
      returning productbasket.map(_.id)
      into {case((basket_id,product_id),id)=>ProductBasket(id,basket_id,product_id)})+=(basket_id,product_id)
  }
  def delete(productbasketId:Int):Future[Unit] = db.run{
    productbasket.filter(_.id === productbasketId).delete.map(_=>())
  }
  def deleteByProductId(productId:Int) = db.run{
    productbasket.filter(_.product_id === productId).delete.map(_=>())
  }
  def deleteByBasketId(basketId:Int) = db.run{
    productbasket.filter(_.basket_id === basketId).delete.map(_=>())
  }
  def update(productbasketId:Int,new_productbasket:ProductBasket):Future[Unit] = {
    val productbasketUpdate = new_productbasket.copy(productbasketId)
    db.run{
      productbasket.filter(_.id === productbasketId).update(productbasketUpdate).map(_=>())
    }
  }

}
