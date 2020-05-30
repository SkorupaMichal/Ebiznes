package modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
class BaseModule extends AbstractModule with ScalaModule with AkkaGuiceSupport{

}
