package http

import cats.*
import cats.data.Kleisli
import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import command.*
import domain.*
import io.circe.*
import io.circe.Decoder.*
import io.circe.Encoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.util.UUID
import scala.util.Try

object Http4sApp extends IOApp:

  import Codecs.{*, given}

  def loggingMiddleware(service: HttpRoutes[IO]): HttpRoutes[IO] =
    Kleisli { req =>
      val requestId = UUID.randomUUID()
      println(s"requestId: $requestId => request to ${req.method} ${req.uri}")
      service(req).map { res =>
        println(s"requestId: $requestId => returned with status: ${res.status}")
        res // manipulate response here if you want
      }
    }

  def gameRoutes[F[_]: Async]: HttpRoutes[F] =
    val dsl = Http4sDsl[F]
    import dsl.*

    val service                        = Http4sGameService[F]
    given EntityDecoder[F, CreateGame] = jsonOf[F, CreateGame]

    HttpRoutes.of[F] {
      case GET -> Root :? PageParam(page) +& PerPageParam(maybePerPage) =>
        val games = service.list(page, maybePerPage.getOrElse(10))
        Ok(games.map(_.asJson))
      case GET -> Root / GameIdVar(gameId)                              =>
        {Ok(service.get(gameId).map(_.asJson))
        }
      case req @ POST -> Root                                           =>
        for
          createGame <- req.as[CreateGame]
          game       <- service.update(createGame.toGame)
          res        <- Created(game.asJson)
        yield res
    }

  override def run(args: List[String]): IO[ExitCode] =

    val apis = Router(
      "/api/games" -> loggingMiddleware(Http4sApp.gameRoutes[IO])
    ).orNotFound

    given LoggerFactory[IO] = Slf4jFactory.create[IO]

    EmberServerBuilder
      .default[IO]
      .withHost(host"localhost")
      .withPort(port"8080")
      .withHttpApp(apis)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)

object Codecs:

  given Codec[GameId] =
    Codec
      .from(decodeUUID, encodeUUID)
      .iemap[GameId](uuid => Right(GameId(uuid))) { case GameId(id) => id }

  given Codec[Publisher] =
    Codec
      .from(decodeString, encodeString)
      .iemap[Publisher](s => Right(Publisher(s))) { case Publisher(name) => name }

  given Codec[Developer] =
    Codec
      .from(decodeString, encodeString)
      .iemap[Developer](s => Right(Developer(s))) { case Developer(name) => name }

  object PageParam extends QueryParamDecoderMatcher[Int]("page")

  object PerPageParam extends OptionalQueryParamDecoderMatcher[Int]("perPage")

  object GameIdVar:

    def unapply(str: String): Option[GameId] =
      Try(UUID.fromString(str)).toOption.map(GameId(_))
