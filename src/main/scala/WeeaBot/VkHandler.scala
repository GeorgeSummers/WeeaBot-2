package WeeaBot

import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.{ExecutorService, Executors}

import com.typesafe.config.{Config, ConfigFactory}
import com.vk.api.sdk.exceptions.{ApiException, ClientException}
import com.vk.api.sdk.objects.messages._
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.White

object VkHandler {

  @throws(classOf[NullPointerException])
  @throws(classOf[ApiException])
  @throws(classOf[InterruptedException])
  def main(args: Array[String]): Unit = {
    println("Running...")
    val confFileName = "cred.conf"
    val config = ConfigFactory.load(confFileName)
    val groupId: Int = config.getInt("groupId")
    val accessToken: String = config.getString("access_token")
    VkEngine.instance(groupId, accessToken)
    while (true) {
      Thread.sleep(300)
      try {
        val message: Message = VkEngine.getMessage
        if (message != null) {
          val exec: ExecutorService = Executors.newCachedThreadPool()
          exec.execute(new Messenger(message))
        }
      } catch {
        case e: ClientException =>
          val rct: Int = 10000
          println(e.getMessage)
          Thread.sleep(rct)
      }
    }
  }

}
