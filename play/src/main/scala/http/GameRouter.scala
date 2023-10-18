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

class GameRouter @Inject() (
    val Action: DefaultActionBuilder,
    service: PlayGameService,
    parser: PlayBodyParsers,
  )(using ExecutionContext
  ) extends SimpleRouter:
  
  import Formats.given

  override def routes: Router.Routes =
    case GET(p"/" ? q"page=${int(page)}"
               & q_o"perPage=${int(maybePerPage)}") =>
      Action.async {
        val perPage = maybePerPage.getOrElse(10)
        service.list(page, perPage).map(Json.toJson(_)).map(Results.Ok(_))
      }
    case POST(p"/")                                                           =>
      Action.async(parser.json[CreateGame]) { req =>
        service.update(req.body.toGame).map(Json.toJson(_)).map(Results.Ok(_))
      }
    case GET(p"/${uuid(id)}")                                                 =>
      Action.async {
        service.get(GameId(id)).map(Json.toJson(_)).map(Results.Ok(_)).recover {
          case EntityNotFound(id) => Results.NotFound(s"Game with id $id not found")
        }
      }

object Formats:
  given Format[GameId] = Format.of[UUID].bimap[GameId](GameId(_), { case GameId(id) => id })

  given Format[Platform] = Format.of[String].bimap[Platform](Platform.valueOf, _.toString)

  given Format[Developer] = Format.of[String].bimap[Developer](Developer(_), _.toString)

  given Format[Publisher] = Format.of[String].bimap[Publisher](Publisher(_), _.toString)

  given OFormat[Game] = Json.format[Game]

  given OFormat[CreateGame] = Json.format[CreateGame]