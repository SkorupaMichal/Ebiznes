package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.concurrent.{Await, ExecutionContext, Future,duration}
import scala.util.{Failure, Success}

case class CreateProductForm(name:String,cost:Int,count:Int,producer:String,categoryId:Int,subcategoryId:Int)
case class UpdateProductForm(id:Int,name:String,cost:Int,count:Int,producer:String,categoryId:Int,subcategoryId:Int)
case class AddProductToBasketForm(basketId:Int);
@Singleton
class ProductController @Inject() (cc:ControllerComponents,dd:MessagesControllerComponents,
                                   subcatRepo:SubCategoryRepository,productRepo:ProductRepository,
                                   commentRepo:CommentRepository,catRepo:CategoryRepository,
                                   basketRepo:BasketRepository,prodbasketRepo:ProductBasketRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd) {
  /*Product controller*/
  val productForm: Form[CreateProductForm] = Form{
    mapping(
      "name" -> nonEmptyText,
      "cost" -> number,
      "count" -> number,
      "producer" ->nonEmptyText,
      "categoryId" -> number,
      "subcategoryId" -> number
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }
  val updateProductForm: Form[UpdateProductForm] = Form{
    mapping(
      "id"  -> number,
      "name" -> nonEmptyText,
      "cost" -> number,
      "count" -> number,
      "producer" ->nonEmptyText,
      "categoryId" -> number,
      "subcategoryId" -> number
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }
  val addProductForm: Form[AddProductToBasketForm] = Form{
    mapping(
      "basketId" -> number,
    )(AddProductToBasketForm.apply)(AddProductToBasketForm.unapply)
  }

  def getProducts = Action.async{ implicit request =>
    productRepo.list().map(
      products=>Ok(views.html.products(products))
    )
  }
  def getPRoductByID(productId:Int) = Action.async{ implicit request=>
    productRepo.getById(productId).map(
      product=> product match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak produktu o podanym id")
      }
    )

  }
  def addProductToBasket(productId:Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var basket:Seq[Basket] =  Seq[Basket]();
    basketRepo.list().onComplete {
      case Success(c) => basket = c
      case Failure(_) => print("fail")
    }
    productRepo.getById(productId).map(b=>Ok(views.html.addproducttobasket(addProductForm,basket,productId)))

  }
  def addProductBasketHandle(productId:Int) = Action.async{ implicit request =>
    var basket:Seq[Basket] =  Seq[Basket]();
    basketRepo.list().onComplete {
      case Success(c) => basket = c
      case Failure(_) => print("fail")
    }
    addProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.addproducttobasket(errorForm,basket,productId))
        )
      },
      prod => {
        prodbasketRepo.create(prod.basketId,productId).map { _ =>
          Redirect(routes.ProductController.getProducts()).flashing("success" -> "product.created")
        }
      }
    )
  }
  def createProduct:Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val subcat = subcatRepo.list()
    Await.result(subcat,duration.Duration.Inf)
    var seqCat = Seq[Category]()
    catRepo.list().onComplete{
      case Success(c) => seqCat = c
      case Failure(_) => print("fail")
    }

    subcat.map(c=>Ok(views.html.productadd(productForm,c,seqCat)))
  }
  def createProductHandle = Action.async { implicit request =>
    var subcat:Seq[SubCategory] = Seq[SubCategory]()
    subcatRepo.list().onComplete{
      case Success(c) => subcat = c
      case Failure(_) =>print("fail")
    }
    var cat:Seq[Category] = Seq[Category]()
    catRepo.list().onComplete{
      case Success(c) => cat = c
      case Failure(_) =>print("fail")
    }
    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productadd(errorForm,subcat,cat))
        )
      },
      prod => {
        productRepo.create(prod.name, prod.cost,prod.count,prod.producer,prod.categoryId, prod.subcategoryId).map { _ =>
          Redirect(routes.ProductController.getProducts()).flashing("success" -> "product.created")
        }
      }
    )
  }

  def updateProduct(productId: Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    var subcat:Seq[SubCategory] =  Seq[SubCategory]();
    subcatRepo.list().onComplete {
      case Success(c) => subcat = c
      case Failure(_) => print("fail")
    }
    var cat:Seq[Category] =  Seq[Category]();
    catRepo.list().onComplete {
      case Success(c) => cat = c
      case Failure(_) => print("fail")
    }
    val products = productRepo.getById(productId)
    products.map(b=>{
      val bForm = updateProductForm.fill(UpdateProductForm(b.head.id,b.head.name,b.head.cost,b.head.count,b.head.producer,b.head.category_id,b.head.subcategory_id))
      Ok(views.html.productupdate(bForm,subcat,cat))
    })
  }

  def updateProductHandle = Action.async { implicit request =>
    var subcat:Seq[SubCategory] = Seq[SubCategory]()
    subcatRepo.list().onComplete{
      case Success(c) => subcat = c
      case Failure(_) =>print("fail")
    }
    var cat:Seq[Category] = Seq[Category]()
    catRepo.list().onComplete{
      case Success(c) => cat = c
      case Failure(_) =>print("fail")
    }
    updateProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productupdate(errorForm,subcat,cat))
        )
      },
      product =>{
        productRepo.update(product.id,Product(product.id,product.name,product.cost,product.count,product.producer,product.categoryId,product.subcategoryId)).map{
          _ => Redirect(routes.ProductController.updateProduct(product.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deleteProduct(productId: Int) = Action{
    Await.result(commentRepo.deleteByProductId(productId),duration.Duration.Inf)
    productRepo.delete(productId)
    Redirect("/products")
  }

  /*Json api*/
  def getProductsJson = Action.async{implicit request=>
    val products = productRepo.list()
    Await.result(products,duration.Duration.Inf)
    products.map(b=>Ok(Json.toJson(b)))
  }
  def getProductsByIdJson(prodId:Int) = Action.async{implicit request=>
    val products = productRepo.getById(prodId)
    Await.result(products,duration.Duration.Inf)
    products.map(b=>Ok(Json.toJson(b)))
  }
  def getProductsByCategoryIdJson(categoryId:Int) = Action.async{implicit request=>
    val products = productRepo.getByCategoryId(categoryId)
    Await.result(products,duration.Duration.Inf)
    products.map(b=>Ok(Json.toJson(b)))
  }
  def getProductsBySubcategoryIdJson(subcatid:Int) = Action.async{implicit request=>
    val products = productRepo.getBySubCategoryId(subcatid)
    Await.result(products,duration.Duration.Inf)
    products.map(b=>Ok(Json.toJson(b)))
  }
  def createProductByJson = Action(parse.json){implicit request=>
    /*id:Int,name:String,cost:Int,count:Int,producer:String,category_id:Int,subcategory_id:Int*/
    val name = (request.body \ "name").as[String]
    val cost = (request.body \ "cost").as[Int]
    val count = (request.body \ "count").as[Int]
    val producer = (request.body \ "producer").as[String]
    val categoryId = (request.body \ "category_id").as[Int]
    val subcategoryId = (request.body \ "subcategory_id").as[Int]
    productRepo.create(name,cost,count,producer,categoryId,subcategoryId)
    Ok("")
  }
  def updateProductByJson(productId:Int) = Action(parse.json){implicit request=>
    val name =  (request.body \ "name").as[String]
    val cost =  (request.body \ "cost").as[Int]
    val count = (request.body \ "count").as[Int]
    val producer = (request.body \ "producer").as[String]
    val categoryId = (request.body \ "category_id").as[Int]
    val subcategoryId = (request.body \ "subcategory_id").as[Int]
    productRepo.update(productId,Product(productId,name,cost,count,producer,categoryId,subcategoryId))
    Ok("")
  }
  def deleteProductByJson(categoryId:Int) = Action{
    Await.result(productRepo.delete(categoryId),duration.Duration.Inf)
    Ok("")
  }

}
