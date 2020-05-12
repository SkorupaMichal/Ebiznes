package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{Await, ExecutionContext, Future, duration}
import scala.util.{Failure, Success}

case class CreateImageForm(url:String,description:String,productId:Int)
case class UpdateImageForm(id:Int,url:String,description:String,productId:Int)

@Singleton
class ImageController @Inject()(cc:ControllerComponents,dd:MessagesControllerComponents,reposImages:ImageRepository,
                                protected val pRepo:ProductRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
  /*Product Image controller*/

  val imageForm: Form[CreateImageForm] = Form{
    mapping(
      "url" -> nonEmptyText,
      "description" ->nonEmptyText,
      "productId" -> number
    )(CreateImageForm.apply)(CreateImageForm.unapply)
  }
  val updateimageForm: Form[UpdateImageForm] = Form{
    mapping(
      "id"  -> number,
      "url" -> nonEmptyText,
      "description" ->nonEmptyText,
      "productId" -> number
    )(UpdateImageForm.apply)(UpdateImageForm.unapply)
  }
  var products:Seq[Product] = Seq[Product]()
  def getProductsSeq = {
    pRepo.list().onComplete{
      case Success(c) => products = c
      case Failure(_) =>print("fail")
    }
  }
  def getImages = Action.async{ implicit request =>
    /* Get all images from database*/
    reposImages.list().map(
      images=>Ok(views.html.images(images))
    )

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
    getProductsSeq
    imageForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.imageadd(errorForm,products))
        )
      },
      image => {
        reposImages.create(image.url, image.description, image.productId).map { _ =>
          Redirect(routes.ImageController.getImages()).flashing("success" -> "image.created")
        }
      }
    )
  }

  def updateImage(imageId: Int) : Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    getProductsSeq
    val image = reposImages.getById(imageId)
    image.map(b=>{
      val bForm = updateimageForm.fill(UpdateImageForm(b.head.id,b.head.url,b.head.description,b.head.productId))
      Ok(views.html.imageupdate(bForm,products))
    })
  }
  def updateImageHandle = Action.async{implicit request=>
    getProductsSeq
    updateimageForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.imageupdate(errorForm,products))
        )
      },
      image =>{
        reposImages.update(image.id,Image(image.id,image.url,image.description,image.productId)).map{
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
  def getImageByProductIdJson(prodId:Int) = Action.async{implicit  request =>
    val images = reposImages.getByProductId(prodId)
    Await.result(images,duration.Duration.Inf)
    images.map(b=>Ok(Json.toJson(b)))
  }
  def getImageFromRequest(request:MessagesRequest[JsValue]):(String,String,Int) = {
    var url = ""
    var description = ""
    var productId = -1
    (request.body \ "url").asOpt[String].map{ur=>
      url = ur
    }.getOrElse(BadRequest("Blad"))
    (request.body \ "description").asOpt[String].map{desc=>
      description = desc
    }.getOrElse(BadRequest("Blad"))
    (request.body \ "product_id").asOpt[Int].map{prodid=>
      productId = prodid
    }.getOrElse(BadRequest("Blad"))
    (url,description,productId)
  }
  def createImageJson = Action(parse.json){implicit request=>
    val image = getImageFromRequest(request)
    reposImages.create(image._1,image._2,image._3)
    Ok("")
  }
  def updateImageJson(imageId:Int) = Action(parse.json){implicit request=>
    val image = getImageFromRequest(request)
    reposImages.update(imageId,Image(imageId,image._1,image._2,image._3))
    Ok("")
  }
  def deleteImageJson(imageId:Int) = Action{
    Await.result(reposImages.delete(imageId),duration.Duration.Inf)
    Ok("")
  }
}
