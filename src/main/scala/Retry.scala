package images.social
import scalaz.Equal
import scala.util.control.NonFatal

trait Retry extends Logging {
  protected def maxRetries: Int = 5
  protected def isIgnorableException: Throwable => Boolean = (t: Throwable) => t.isInstanceOf[Throwable] && NonFatal(t)

  final def retryAction(action: => Boolean): Option[Boolean] = retryAction[Boolean](action)(_ == true)

  final def retryAction[T <: AnyVal, R >: T: Equal](action: => T)(matches: => T): Option[R] = {
    def compare(other: R): Boolean = Equal[R].equal(matches, other)
    retryAction[R](action)(compare)
  }

  final def retryAction[T](action: => T)(matches: T => Boolean): Option[T] = {
    def go(count: Int = maxRetries): Option[T] = {
      if (count <= 0) {
        None
      } else {
        try {
          val result = action
          if (matches(result)) {
            Some(result)
          } else {
            println(s"Retrying action that gave result ${result} because it didn't match the predicate.")
            go(count - 1)
          }
        } catch {
          case err: Throwable if isIgnorableException(err) =>
            println(s"Retrying action because of caught and ignorable exception.", err)
            go(count - 1)

          case err: Throwable =>
            println(s"Got a fatal exception, exiting retry.", err)
            None
        }
      }
    }
    go()
  }
}
