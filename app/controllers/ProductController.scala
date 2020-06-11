package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsSuccess, JsValue, Json}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.Silhouette
import utils.auth.{HasRole,DefaultEnv}

import scala.concurrent.{Await, ExecutionContext, Future, duration}
import scala.util.{Failure, Success}

case class CreateProductForm(name:String,cost:Int,count:Int,producer:String,categoryId:Int,subcategoryId:Int)
case class UpdateProductForm(id:Int,name:String,cost:Int,count:Int,producer:String,categoryId:Int,subcategoryId:Int)
case class AddProductToBasketForm(basketId:Int);
@Singleton
class ProductController @Inject() (cc:ControllerComponents,dd:MessagesControllerComponents,
                                   subcatRepo:SubCategoryRepository,productRepo:ProductRepository,
                                   commentRepo:CommentRepository,catRepo:CategoryRepository,
                                   basketRepo:BasketRepository,prodbasketRepo:ProductBasketRepository,
                                   silhouette:Silhouette[DefaultEnv])(implicit ex:ExecutionContext) extends MessagesAbstractController(dd) {
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
  var baskets:Seq[Basket] = Seq[Basket]()
  var categories:Seq[Category] = Seq[Category]()
  var subcategories:Seq[SubCategory] = Seq[SubCategory]()
  def getBasketsSeq = {
    basketRepo.list().onComplete {
      case Success(c) => baskets = c
      case Failure(_) => print("fail")
    }
  }
  def getCategoriesSeq = {
    catRepo.list().onComplete{
      case Success(c) => categories = c
      case Failure(_) => print("fail")
    }
  }
  def getSubCategoriesSeq = {
    subcatRepo.list().onComplete{
      case Success(c) => subcategories = c
      case Failure(_) =>print("fail")
    }
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
    getBasketsSeq
    productRepo.getById(productId).map(b=>Ok(views.html.addproducttobasket(addProductForm,baskets,productId)))

  }
  def addProductBasketHandle(productId:Int) = Action.async{ implicit request =>
    getBasketsSeq
    addProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.addproducttobasket(errorForm,baskets,productId))
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
    getCategoriesSeq

    subcat.map(c=>Ok(views.html.productadd(productForm,c,categories)))
  }
  def createProductHandle = Action.async { implicit request =>
    getSubCategoriesSeq
    getCategoriesSeq
    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productadd(errorForm,subcategories,categories))
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
    getSubCategoriesSeq
    getCategoriesSeq
    val products = productRepo.getById(productId)
    products.map(b=>{
      val bForm = updateProductForm.fill(UpdateProductForm(b.head.id,b.head.name,b.head.cost,b.head.count,b.head.producer,b.head.categoryId,b.head.subcategoryId))
      Ok(views.html.productupdate(bForm,subcategories,categories))
    })
  }

  def updateProductHandle = Action.async { implicit request =>
    getSubCategoriesSeq
    getCategoriesSeq
    updateProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productupdate(errorForm,subcategories,categories))
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
  def getProductFromRequest(request:MessagesRequest[JsValue]):(String,Int,Int,String,Int,Int) = {
    var name = ""
    var cost = -1
    var count = -1
    var producer = ""
    var cateogryId = -1
    var subcateogryId = -1
    (request.body \ "name").asOpt[String].map{ na=>
      name = na
    }
    (request.body \ "cost").asOpt[Int].map{ co=>
      cost = co
    }
    (request.body \ "count").asOpt[Int].map{ con=>
      count = con
    }
    (request.body \ "producer").asOpt[String].map{ prod=>
      producer = prod
    }
    (request.body \ "cateogryId").asOpt[Int].map{ catid=>
      cateogryId = catid
    }
    (request.body \ "subcateogryId").asOpt[Int].map{ subcatid=>
      subcateogryId = subcatid
    }
    (name,cost,count,producer,cateogryId,subcateogryId)
  }
  def createProductByJson = silhouette.SecuredAction(HasRole(UserRoles.Admin)).async(parse.json){implicit request: SecuredRequest[DefaultEnv, JsValue]=>
    /*id:Int,name:String,cost:Int,count:Int,producer:String,category_id:Int,subcategory_id:Int*/
    request.body.validate[Product] match {
      case JsSuccess(json, _) => productRepo.create(json.name,json.cost,json.count,json.producer,json.categoryId,json.subcategoryId).map(_=>Ok("Product add"))
      case _ => Future(InternalServerError("Bad json from product"))
    }

  }
  def updateProductByJson(productId:Int) = Action(parse.json){implicit request=>
    val product = getProductFromRequest(request)
    productRepo.update(productId,Product(productId,product._1,product._2,product._3,product._4,product._5,product._6))
    Ok("")
  }
  def deleteProductByJson(categoryId:Int) = Action{
    Await.result(productRepo.delete(categoryId),duration.Duration.Inf)
    Ok("")
  }

}
