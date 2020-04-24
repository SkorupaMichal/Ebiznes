package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.concurrent.{Await, ExecutionContext, Future,duration}
import scala.util.{Failure, Success}

case class CreateProductForm(name:String,cost:Int,count:Int,producer:String,category_id:Int,subcategory_id:Int)
case class UpdateProductForm(id:Int,name:String,cost:Int,count:Int,producer:String,category_id:Int,subcategory_id:Int)
@Singleton
class ProductController @Inject() (cc:ControllerComponents,dd:MessagesControllerComponents,
                                   subcatRepo:SubCategoryRepository,productRepo:ProductRepository,
                                   commentRepo:CommentRepository,catRepo:CategoryRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd) {
  /*Product controller*/
  val productForm: Form[CreateProductForm] = Form{
    mapping(
      "name" -> nonEmptyText,
      "cost" -> number,
      "count" -> number,
      "producer" ->nonEmptyText,
      "category_id" -> number,
      "subcategory_id" -> number
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }
  val updateProductForm: Form[UpdateProductForm] = Form{
    mapping(
      "id"  -> number,
      "name" -> nonEmptyText,
      "cost" -> number,
      "count" -> number,
      "producer" ->nonEmptyText,
      "category_id" -> number,
      "subcategory_id" -> number
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }
  def getProducts = Action.async{ implicit request =>
    productRepo.list().map(
      products=>Ok(views.html.products(products))
    )
    //Ok("Product")
  }
  def getPRoductByID(productId:Int) = Action.async{ implicit request=>
    productRepo.getById(productId).map(
      product=> product match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak produktu o podanym id")
      }
    )

  }
  def createProduct:Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val subcat = subcatRepo.list()
    Await.result(subcat,duration.Duration.Inf)
    var seqCat = Seq[Category]()
    var cat = catRepo.list().onComplete{
      case Success(c) => seqCat = c
      case Failure(_) => print("fail")
    }

    subcat.map(c=>Ok(views.html.productadd(productForm,c,seqCat)))
  }
  def createProductHandle = Action.async { implicit request =>
    var subcat:Seq[SubCategory] = Seq[SubCategory]()
    val subcategories = subcatRepo.list().onComplete{
      case Success(c) => subcat = c
      case Failure(_) =>print("fail")
    }
    var cat:Seq[Category] = Seq[Category]()
    val categories = catRepo.list().onComplete{
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
        productRepo.create(prod.name, prod.cost,prod.count,prod.producer,prod.category_id, prod.subcategory_id).map { _ =>
          Redirect(routes.ProductController.getProducts()).flashing("success" -> "product.created")
        }
      }
    )
  }

  def updateProduct(productId: Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    var subcat:Seq[SubCategory] =  Seq[SubCategory]();
    val subcategories = subcatRepo.list().onComplete {
      case Success(c) => subcat = c
      case Failure(_) => print("fail")
    }
    var cat:Seq[Category] =  Seq[Category]();
    val categories = catRepo.list().onComplete {
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
    val subcategories = subcatRepo.list().onComplete{
      case Success(c) => subcat = c
      case Failure(_) =>print("fail")
    }
    var cat:Seq[Category] = Seq[Category]()
    val categories = catRepo.list().onComplete{
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
        productRepo.update(product.id,Product(product.id,product.name,product.cost,product.count,product.producer,product.category_id,product.subcategory_id)).map{
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
}
