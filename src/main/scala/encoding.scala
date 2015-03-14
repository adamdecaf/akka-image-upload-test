package images.social
import java.util.{Base64 => JBase64}

object Base64 {
  final def apply(str: String): String = apply(str.getBytes("UTF-8"))
  final def apply(arr: Array[Byte]): String = JBase64.getEncoder().encodeToString(arr)
}
