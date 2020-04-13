package controllers
import javax.inject._
import play.api.mvc._

@Singleton
class StatisticsController @Inject() (cc:ControllerComponents) extends AbstractController(cc){

  def getStatistics = Action{
    Ok("Delivery" )
  }

  def createStatistic = Action{
    Ok("Create Delivery" )
  }

  def updateStatistic(statisticId: Int) = Action{
    Ok("Update Delivery")
  }

  def deleteStatistic(statisticId: Int) = Action{
    Ok("Delete Delivery")
  }

}
