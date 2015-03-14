package images.social
import java.security.MessageDigest

object SHA256 {
  def apply(data: String): String = {
    val hashInstance = MessageDigest.getInstance("SHA-256")
    Base64(hashInstance.digest(data.getBytes("UTF-8")))
  }
}
