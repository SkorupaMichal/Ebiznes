package models
import javax.inject._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.SQLiteProfile.api._

@Singleton
class UserRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(implicit executionContext: ExecutionContext){
  
}
