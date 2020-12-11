package WeeaBot

import scala.jdk.CollectionConverters._
import com.vk.api.sdk.client.{ApiRequest, VkApiClient}
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.exceptions._
import com.vk.api.sdk.objects.messages.Message
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery




class VkEngine(groupId: Int, accessToken: String) {
    //private val APP_ID: Int = 7694238
  private var transportClient: HttpTransportClient = _
  private var vk: VkApiClient = _
  private var actor: GroupActor = _
  private var ts = 0
  private var maxMsgId = -1

  // TODO ^^^terrible, rethink later
  try {
    transportClient = HttpTransportClient.getInstance
    vk = new VkApiClient(transportClient)
    actor = new GroupActor(groupId, accessToken)
    ts = vk.messages.getLongPollServer(actor).execute.getTs
    println(ts)
  } catch {
    case e: Exception => println(s"${e.getMessage}")
  }

  // copypasta => vk.groups().setLongPollSettings(actor, groupId).enabled(true).messageNew(true).execute()

  @throws(classOf[ClientException])
  @throws(classOf[ApiException ])
  def getMessage: Message = {
    //TODO change to pattern-matching
    val eventsQuery: MessagesGetLongPollHistoryQuery = vk.messages().getLongPollHistory(actor).ts(ts)
    if (maxMsgId > 0) eventsQuery.maxMsgId(maxMsgId)
    val messages: List[Message] = eventsQuery.execute()
      .getMessages.getItems
      .asScala.toList
    if (messages.nonEmpty) {
      try {
        ts = vk.messages().getLongPollServer(actor).execute().getTs
      } catch {
        case e: ClientException => println(e.getMessage)
      }
    }
    if(messages.nonEmpty && !messages.asJava.get(0).isOut){
      val messageId = messages.asJava.get(0).getId
      if(messageId > maxMsgId)  maxMsgId = messageId
      messages.asJava.get(0)
    }
    null
  }
}

object VkEngine {
  private var _instance: VkEngine = null

  def instance(groupId: Int, accessToken: String): VkEngine = {
    if (_instance == null) {
      try {
        _instance = new VkEngine(groupId, accessToken)
      } catch {
        case e: ApiException => println(e.getMessage)
        case e: ClientException => println(e.getMessage)
      }
    }
    _instance
  }
  def terminate(): Unit = _instance = null
  def getMessage: Message = try {
    _instance.getMessage
  } catch {
    case e:NullPointerException => null
  }
}
