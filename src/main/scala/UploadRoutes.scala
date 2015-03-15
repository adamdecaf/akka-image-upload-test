package images.social
import akka.http.model._
import akka.http.unmarshalling._
import akka.http.server.StandardRoute
import scala.util.Try
import scala.concurrent.Future
import java.io.File
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.io.FileNotFoundException
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.global

// todo
// redirect back to home page w/ image link?
// Spawn off another Future that will upload to S3 (under s3://images.social/images/)
// Write metadata to .json file in S3 (under s3://images.social/metadata/)
// nginx cache and proxy to S3

trait UploadRoutes extends HttpDirectives with JsonMarshalling with Logging {
  lazy val bucket = "images.social"

  def uploadRoutes(): Route =
    (post & path("upload") & entity(as[Multipart.FormData])) { formData =>
      val promise = Promise[Uri]()

      formData.parts.take(1).runForeach { part =>
        val maybeUri = uploadPart(part)
        maybeUri.fold(promise.failure(new Throwable{}))(uri => promise.success(uri))
      }

      val fut = promise.future
      onComplete(fut) {
        case scala.util.Success(uri) => redirect(uri, StatusCodes.Found)
        case _ => complete(StatusCodes.NoContent)
      }
    }

  private[this] def uploadPart(part: Multipart.FormData.BodyPart): Option[Uri] = {
    val originalFilename = part.headers.find { h =>
      h.is("content-disposition")
    } map (_.value.split("filename=").last) getOrElse "Unknown"

    val filename = SHA256(Uuid()).take(8)
    val extension = originalFilename.dropWhile(_ != '.').takeWhile(_ != ';')
    val fullFilename = s"${filename}${extension}"

    // todo: check other parts of the entity
    if (!part.entity.isKnownEmpty) {
      part.entity.dataBytes.runForeach { byteString =>
        val contentType = part.entity.contentType

        val chunks = new ByteArrayInputStream(byteString.toArray)
        val file = new File(fullFilename)
        val output = new FileOutputStream(file)

        try {
          // Read one byte at a time, yea I know..
          Iterator.continually(chunks.read).takeWhile(_ != -1).foreach { chunk =>
            println("read chunk")
            output.write(chunk)
          }

          println("Uploading ${fullFilename} to s3...")
          val result = S3Client.create.putItem(bucket, fullFilename, file)
          log.debug(s"Putting the file from ${fullFilename} result = ${result}.")

        } catch {
          case err: FileNotFoundException =>
            log.error(s"Can't find file ${fullFilename}", err)
        } finally {
          output.flush()
          output.close()
        }
      }
      Some(Uri(s"/images/${fullFilename}"))
    } else {
      None
    }
  }
}
