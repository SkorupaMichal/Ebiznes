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
    def basketId = column[Int]("basket_id")
    def basketFk = foreignKey("basket_fk",basketId,baskets)(_.id,onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    def productId = column[Int]("product_id")
    def productFk = foreignKey("product_fk",productId,products)(_.id,onUpdate = ForeignKeyAction.Restrict,onDelete = ForeignKeyAction.Cascade)
    def * = (id,basketId,productId)<>((ProductBasket.apply _).tupled,ProductBasket.unapply)
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
        cbp.productId === products.id &&
        cbp.basketId === bb.id
    }
    def query = for{
      ((productxbasket,products),bb) <- sequence
    } yield(productxbasket.basketId,productxbasket.productId,products.name,products.cost)
    query.result
  }
  def create(basketId:Int,productId:Int):Future[ProductBasket] = db.run{
    (productbasket.map(c=>(c.basketId,c.productId))
      returning productbasket.map(_.id)
      into {case((basketId,productId),id)=>ProductBasket(id,basketId,productId)})+=(basketId,productId)
  }
  def delete(productbasketId:Int):Future[Unit] = db.run{
    productbasket.filter(_.id === productbasketId).delete.map(_=>())
  }
  def deleteByProductId(productId:Int) = db.run{
    productbasket.filter(_.productId === productId).delete.map(_=>())
  }
  def deleteByBasketId(basketId:Int) = db.run{
    productbasket.filter(_.basketId === basketId).delete.map(_=>())
  }
  def update(productbasketId:Int,newProductbasket:ProductBasket):Future[Unit] = {
    val productbasketUpdate = newProductbasket.copy(productbasketId)
    db.run{
      productbasket.filter(_.id === productbasketId).update(productbasketUpdate).map(_=>())
    }
  }

}
