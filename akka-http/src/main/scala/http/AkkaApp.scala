package http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.directives.LogEntry
import command.CreateGame
import domain.*
import error.EntityNotFound
import play.api.libs.json.*

import java.util.UUID
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.*

@main
def main(): Unit =

  given system: ActorSystem[Nothing]               = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  given executionContext: ExecutionContextExecutor = system.executionContext
  
  import Formats.given

  // service
  val service = AkkaGameService()

  // no logback set up, so nothing will be logged for now
  val requestLogging =
    logRequest((req: HttpRequest) => {
      LogEntry(s"${req.method} ${req.uri}", akka.event.Logging.InfoLevel)
    })

  val route         =
    pathPrefix("api" / "games") {
      concat(
        get {
          parameters("page".as[Int], "perPage".as[Int].optional) { (page, maybePerPage) =>
            complete {
              val perPage = maybePerPage.getOrElse(10)
              service.list(page, perPage).map(Json.toJson(_).toString)
            }
          }
        },
        post {
          entity(as[String].map(Json.parse(_).as[CreateGame])) { crateGame =>
            complete {
              service.update(crateGame.toGame).map(Json.toJson(_).toString)
            }
          }
        },
        path(JavaUUID) { id =>
          get {
            val result = service.get(GameId(id)).map(Json.toJson(_).toString)
            onComplete(result) {
              case Success(game)               =>
                complete(game)
              case Failure(EntityNotFound(id)) =>
                complete(StatusCodes.NotFound, s"Game with id $id not found")
              case Failure(exception)          =>
                complete(StatusCodes.InternalServerError, exception.getMessage)
            }
          }
        },
      )
    }
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(requestLogging(route))

  println(
    s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop..."
  )
  StdIn.readLine()                       // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind())                 // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done


object Formats:
  given Format[GameId] = Format.of[UUID].bimap[GameId](GameId(_), { case GameId(id) => id })

  given Format[Platform] = Format.of[String].bimap[Platform](Platform.valueOf, _.toString)

  given Format[Developer] = Format.of[String].bimap[Developer](Developer(_), _.toString)

  given Format[Publisher] = Format.of[String].bimap[Publisher](Publisher(_), _.toString)

  given OFormat[Game] = Json.format[Game]

  given OFormat[CreateGame] = Json.format[CreateGame]