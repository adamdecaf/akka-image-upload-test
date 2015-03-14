package images.social
import javax.xml.bind.DatatypeConverter

object Base64 {
  def apply(bytes: Array[Byte]): String =
    DatatypeConverter.printBase64Binary(bytes)

  def apply(str: String): String =
    apply(str.getBytes("UTF-8"))
}
