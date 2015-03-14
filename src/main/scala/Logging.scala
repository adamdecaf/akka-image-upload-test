package images.social
import org.slf4j.LoggerFactory

trait Logging { self =>
  protected val log = LoggerFactory.getLogger(self.getClass)
}
