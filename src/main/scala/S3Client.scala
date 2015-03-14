package images.social
import java.io.{ByteArrayInputStream, File, InputStream}
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import org.apache.commons.io.IOUtils

object S3Client {
  lazy val create: S3Client = new S3Client(AWSConfig.awsCredentials, AWSConfig.awsConfig)
}

final class S3Client(credentials: BasicAWSCredentials, clientConfig: ClientConfiguration) extends Logging with Retry with WithConfig {
  private[this] val maxRetries = config.getConfigInt("aws.s3.max-retries")
  private[this] val client = new AmazonS3Client(credentials, clientConfig)

  def bucketExists(name: String): Boolean = name.nonEmpty && client.doesBucketExist(name)

  def getItemContentAsStream(bucket: String, key: String): Option[InputStream] =
    WithS3Object(client, bucket, key) { obj =>
      new ByteArrayInputStream(IOUtils.toByteArray(obj.getObjectContent))
    }

  def getItemContentAsString(bucket: String, key: String): Option[String] =
    WithS3Object(client, bucket, key) { obj =>
      IOUtils.toString(obj.getObjectContent)
    }

  def putItem(bucket: String, key: String, content: File): Boolean = {
    val req = new PutObjectRequest(bucket, key, content)

    def attempt(tries: Int = 0): Boolean = {
      if (tries >= maxRetries) {
        log.warn(s"Giving up trying to put file from '${content.getAbsolutePath}' to s3 in bucket: '${bucket}' and key: '${key}'.")
        false
      } else {
        try {
          client.putObject(req)
          true
        } catch {
          case err: AmazonS3Exception =>
            log.warn("Got an exception from S3 when putting an object scheduling for a retry.", err)
            attempt(tries + 1)
        }

      }
    }

    attempt()
  }

  def putItemUntilReadPasses(bucket: String, key: String, content: File, waitMillis: Long = 500L): Boolean = {
    if (putItem(bucket, key, content)) {

      def reader: Boolean = {
        Thread.sleep(waitMillis)
        val maybeStream = getItemContentAsStream(bucket, key)
        try {
          maybeStream.exists(_.available > 0)
        } finally {
          maybeStream.foreach(_.close)
        }
      }

      retryAction(reader).exists(_ == true)
    } else {
      log.warn(s"Unable to put item '${key}' into bucket '${bucket}' before we can even wait until the read on it passes.")
      false
    }
  }

  def deleteItems(bucket: String, itemNames: String*): Unit = {
    if (bucket.nonEmpty) {
      itemNames.filter(_.nonEmpty).foreach { key =>
        client.deleteObject(bucket, key)
      }
    }
  }
}

object WithS3Object extends Logging {
  def apply[T](client: AmazonS3Client, bucket: String, key: String, maxRetries: Int = 3)(f: S3Object => T): Option[T] = {
    val request = new GetObjectRequest(bucket, key)

    def attempt(tries: Int = 0): Option[T] = {
      if (tries >= maxRetries) {
        log.warn(s"Giving up trying to s3 S3Object under bucket: '${bucket}' and key: '${key}' due to too many retries.")
        None
      } else {
        try {
          val obj = client.getObject(request)
          val result = f(obj)
          obj.close
          Some(result)
        } catch {
          case err: AmazonS3Exception =>
            if (err.getMessage.contains("The specified key does not exist")) {
              log.debug(s"The object under '${key}' in bucket '${bucket}' doesn't exist.")
              None
            } else {
              log.warn("Got an exception from S3 when reading an object scheduling for a retry.", err)
              attempt(tries + 1)
            }
        }
      }
    }

    attempt()
  }
}
