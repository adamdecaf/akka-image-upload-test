package images.social
import java.util.UUID

object Uuid {
  def apply(): String = UUID.randomUUID().toString
}
