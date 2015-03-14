package images.social

object ShutdownHook {
  def apply(desc: String)(thing: => Unit) {
    val thread = new Thread {
      override def run: Unit = {
        // printlns incase loggers are shutdown
        println(s"Shutting down gracefully, waiting for ${desc}")
        thing
        println(s"${desc} done!")
      }
    }
    Runtime.getRuntime.addShutdownHook(thread)
  }
}
