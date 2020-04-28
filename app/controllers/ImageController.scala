package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.concurrent.{Await, ExecutionContext, Future,duration}
import scala.util.{Failure, Success}

case class CreateImageForm(url:String,description:String,product_id:Int)
case class UpdateImageForm(id:Int,url:String,description:String,product_id:Int)

@Singleton
class ImageController @Inject()(cc:ControllerComponents,dd:MessagesControllerComponents,reposImages:ImageRepository,
                                protected val pRepo:ProductRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
  /*Product Image controller*/

  val imageForm: Form[CreateImageForm] = Form{
    mapping(
      "url" -> nonEmptyText,
      "description" ->nonEmptyText,
      "product_id" -> number
    )(CreateImageForm.apply)(CreateImageForm.unapply)
  }
  val updateimageForm: Form[UpdateImageForm] = Form{
    mapping(
      "id"  -> number,
      "url" -> nonEmptyText,
      "description" ->nonEmptyText,
      "product_id" -> number
    )(UpdateImageForm.apply)(UpdateImageForm.unapply)
  }
  def getImages = Action.async{ implicit request =>
    /* Get all images from database*/
    reposImages.list().map(
      images=>Ok(views.html.images(images))
    )
    //Ok("Comments" )
  }
  def getImagesById(imageId:Int) = Action.async(implicit request=>{
      reposImages.getById(imageId).map(
        image => image match{
          case Some(i) => Ok(views.html.images(Seq[Image](i)))
          case None => Ok("Brak obrazu o podanym id")
        }
      )
  })

  def createImage:Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val products = pRepo.list()
    products.map(prod =>Ok(views.html.imageadd(imageForm,prod)))
  }
  def createImageHandle = Action.async { implicit request =>
    var prod:Seq[Product] = Seq[Product]()
    val products = pRepo.list().onComplete{
      case Success(c) => prod = c
      case Failure(_) =>print("fail")
    }
    imageForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.imageadd(errorForm,prod))
        )
      },
      image => {
        reposImages.create(image.url, image.description, image.product_id).map { _ =>
          Redirect(routes.ImageController.getImages()).flashing("success" -> "image.created")
        }
      }
    )
  }

  def updateImage(imageId: Int) : Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    var prod:Seq[Product] =  Seq[Product]();
    val products = pRepo.list().onComplete {
      case Success(c) => prod = c
      case Failure(_) => print("fail")
    }
    val image = reposImages.getById(imageId)
    image.map(b=>{
      val bForm = updateimageForm.fill(UpdateImageForm(b.head.id,b.head.url,b.head.description,b.head.product_id))
      Ok(views.html.imageupdate(bForm,prod))
    })
  }
  def updateImageHandle = Action.async{implicit request=>
    var prod:Seq[Product] = Seq[Product]()
    val products = pRepo.list().onComplete{
      case Success(c) => prod = c
      case Failure(_) => print("fail")
    }
    updateimageForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.imageupdate(errorForm,prod))
        )
      },
      image =>{
        reposImages.update(image.id,Image(image.id,image.url,image.description,image.product_id)).map{
          _ => Redirect(routes.ImageController.updateImage(image.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deleteImage(imageId: Int) = Action{
    reposImages.delete(imageId)
    Redirect("/images")
  }

  /*Json api*/
  def getImagesJson = Action.async{implicit request=>
    val images = reposImages.list()
    Await.result(images,duration.Duration.Inf)
    images.map(b=>Ok(Json.toJson(b)))
  }
  def getImageByIdJson(imageId:Int) = Action.async{implicit request=>
    val images = reposImages.getById(imageId)
    Await.result(images,duration.Duration.Inf)
    images.map(b=>Ok(Json.toJson(b)))
  }
  def getImageByProductId(prodId:Int) = Action.async{implicit  request =>
    val images = reposImages.getByProductId(prodId)
    Await.result(images,duration.Duration.Inf)
    images.map(b=>Ok(Json.toJson(b)))
  }
}
