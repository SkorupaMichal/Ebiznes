package modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import models.daos.{LoginInfoDAO,LoginInfoDAOImpl}
import com.google.inject.{Provides}
import javax.inject.Named
import play.api.Configuration
class BaseModule extends AbstractModule with ScalaModule with AkkaGuiceSupport{
  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[LoginInfoDAO].to[LoginInfoDAOImpl]

  }

  @Named("SendGridApiKey")
  @Provides
  def providesSendGridApiKey(conf: Configuration): String = {
    conf.get[String]("sendgrid.api.key")
  }
}
