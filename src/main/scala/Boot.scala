package images.social

object Boot extends App with HttpServer
    with UploadRoutes {

  println("images.social-api is starting")

  startHttpServer()

  override final def system = ImagesActorSystem
  override final def routes = uploadRoutes()

  println("let's rock and roll!")
}
