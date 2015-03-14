package images.social
import com.typesafe.config.{ConfigFactory, Config => TConfig}
import scala.concurrent.duration.Duration
import scala.collection.JavaConverters._

trait WithConfig {
  lazy val config: Config = TypesafeConfig.config
}

trait Config {
  def getConfigString(key: String): String
  def getConfigInt(key: String): Int
  def getConfigLong(key: String): Long
  def getConfigBoolean(key: String): Boolean
  def getConfigDuration(key: String): Duration
  def getConfigStringList(key: String): List[String]
  def getConfigIntList(key: String): List[Int]
  def pathExists(key: String): Boolean
}

class TypesafeConfig extends Config {
  @volatile protected var underlying: TConfig = fallback

  def getConfigString(key: String): String = underlying.getString(key)
  def getConfigInt(key: String): Int = underlying.getInt(key)
  def getConfigLong(key: String): Long = underlying.getLong(key)
  def getConfigBoolean(key: String): Boolean = underlying.getBoolean(key)
  def getConfigDuration(key: String): Duration = Duration(getConfigString(key))
  def getConfigStringList(key: String): List[String] = underlying.getStringList(key).asScala.toList
  def getConfigIntList(key: String): List[Int] = underlying.getIntList(key).asScala.toList.map(_.toInt)
  def pathExists(key: String): Boolean = underlying.hasPath(key)

  protected[this] lazy val fallback = ConfigFactory.load()
}

object TypesafeConfig {
  def config = new TypesafeConfig()
}
