package images.social
import jsonz._
import jsonz.joda._
import scalaz.NonEmptyList
import scala.concurrent.{Future, ExecutionContext}
import akka.util.ByteString
import akka.stream.scaladsl.{Source, Sink}
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import akka.http.unmarshalling.{FromEntityUnmarshaller, PredefinedFromStringUnmarshallers}
import akka.http.marshalling.{Marshaller, Marshalling, ToResponseMarshaller, ToResponseMarshallable}
import akka.http.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import akka.http.model.{ContentTypes, ContentTypeRange, HttpEntity, HttpRequest, HttpResponse, MediaTypes}

trait JsonMarshalling extends DefaultFormats with ProductFormats with JodaTimeFormats with WithConfig
    with PredefinedFromStringUnmarshallers {

  protected val Jsonz = jsonz.Jsonz

  // RFC4627 defines JSON to always be UTF encoded, we always render JSON to UTF-8
  implicit def writesMarshaller[T: Writes]: ToResponseMarshaller[T] = {
    val contentType = ContentTypes.`application/json`
    val mediaType = MediaTypes.`application/json`

    Marshaller.strict[T, HttpResponse] { obj =>
      Marshalling.WithFixedCharset(mediaType,
                                   contentType.charset,
                                   () => HttpResponse(
                                     entity = HttpEntity(Jsonz.toJsonBytes(obj)).withContentType(contentType)
                                   )
      )
    }
  }

  implicit def readsUnMarshaller[T: Reads](implicit f: ActorFlowMaterializer, ec: ExecutionContext, str: FromEntityUnmarshaller[String]): FromEntityUnmarshaller[T] = {
    str.flatMap { body =>
      Jsonz.fromJsonStr[T](body) match {
        case scalaz.Success(model) => Future.successful(model)
        case scalaz.Failure(err) => Future.failed(new MalformedContent(err))
      }
    }
  }

  implicit def toResponseMarshallable[T: ToResponseMarshaller](obj: T): ToResponseMarshallable = ToResponseMarshallable[T](obj)

  private[this] class MalformedContent(errors: NonEmptyList[JsFailure]) extends Exception {
    override def getMessage(): String = errors.list.mkString
  }
}
