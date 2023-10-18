package http

import com.google.inject.*
import domain.*
import command.*
import error.EntityNotFound
import play.api.*
import play.api.inject.*
import play.api.inject.guice.{GuiceApplicationLoader, GuiceableModule}
import play.api.libs.json.*
import play.api.mvc.*
import play.api.routing.*
import play.api.routing.sird.*
import repo.GameRepo

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext

@Singleton
class ScalaRoutesProvider @Inject() (
  router: GameRouter
) extends Provider[Router]:
  lazy val get = router.withPrefix("/api/games")

class ApplicationLoader extends GuiceApplicationLoader:

  protected override def overrides(context: ApplicationLoader.Context): Seq[GuiceableModule] =
    super
      .overrides(context)
      :+ (inject.bind[Router].toProvider[ScalaRoutesProvider]: GuiceableModule)
      :+ (inject.bind[GameRepo].toProvider[GameRepoProvider]: GuiceableModule)
