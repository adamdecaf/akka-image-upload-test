package images.social
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import java.util.concurrent.TimeUnit

object AWSConfig extends WithConfig {
  final lazy val accessKey = AWSAccessKeys.accessKey
  final lazy val secretAccessKey = AWSAccessKeys.secretAccessKey
  final lazy val awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey)

  lazy val awsConfig = new ClientConfiguration()
    .withConnectionTimeout(timeout("aws.connection-timeout"))
    .withMaxConnections(config.getConfigInt("aws.max-connections"))
    .withMaxErrorRetry(config.getConfigInt("aws.max-error-retry"))
    .withSocketTimeout(timeout("aws.socket-timeout"))

  private[this] def timeout(key: String): Int = config.getConfigDuration(key).toMillis.toInt
}


object AWSAccessKeys extends WithConfig {
  final lazy val accessKey: String = {
    Option(System.getenv("AWS_ACCESS_KEY_ID")) getOrElse
    config.getConfigString("aws.access-key")
  }

  final lazy val secretAccessKey: String = {
    Option(System.getenv("AWS_SECRET_ACCESS_KEY")) getOrElse
    config.getConfigString("aws.secret-access-key")
  }

  lazy val awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey)
}
