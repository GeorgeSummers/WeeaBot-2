package WeeaBot

import com.vk.api.sdk.objects.messages.Message

trait Command {
  val name:String

  def exec(message:Message):Unit

  override def toString:String = s"name: ${this.name}"

  override def hashCode(): Int = this.name.hashCode()
  //TODO check is this fucked up
  override def equals(that: Any): Boolean = that match {
    case that:Command  => if (name.equals(that.name)) true else false
    case _ => false
  }

}
