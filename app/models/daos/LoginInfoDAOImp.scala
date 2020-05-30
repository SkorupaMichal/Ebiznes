package models.daos

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

/**
 * Give access to the user object.
 */
class LoginInfoDAOImp @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends LoginInfoDAO with DAO{

}
