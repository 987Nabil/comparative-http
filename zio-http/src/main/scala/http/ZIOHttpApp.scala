package http

import command.CreateGame
import domain.*
import repo.*
import zio.*
import zio.http.*
import zio.http.codec.PathCodec
import zio.json.*

import java.time.Instant

object ZIOHttpApp extends ZIOAppDefault:

  val gameRepoLayer: ULayer[GameRepo] = ZLayer.succeed(InMemoryGameRepo)

  given JsonCodec[GameId] =
    JsonCodec.uuid.transform[GameId](GameId(_), { case GameId(id) => id })

  given JsonCodec[Publisher] =
    JsonCodec.string.transform[Publisher](Publisher(_), { case Publisher(publisher) => publisher })

  given JsonCodec[Developer] =
    JsonCodec.string.transform[Developer](Developer(_), { case Developer(developer) => developer })

  given JsonCodec[Platform] = JsonCodec.string.transform(Platform.valueOf, _.toString)

  given JsonCodec[Game]       = DeriveJsonCodec.gen[Game]
  given JsonCodec[CreateGame] = DeriveJsonCodec.gen[CreateGame]

  private val gameIdPath: PathCodec[GameId] =
    zio.http.uuid("gameId").transform(GameId(_))({ case GameId(id) => id })

  private val requestLogging = HandlerAspect.interceptHandlerStateful(
    Handler.fromFunctionZIO[Request] { request =>
      zio.Clock.instant.map(now => ((now, request), (request, ())))
    })(Handler.fromFunctionZIO[((java.time.Instant, Request), Response)] {
    case ((now, request), response) =>
      ZIO.logAnnotate(Set(
        LogAnnotation("method", request.method.toString),
        LogAnnotation("path", request.url.path.toString),
        LogAnnotation("status", response.status.code.toString),
        LogAnnotation("duration", s"${(Instant.now.toEpochMilli - now.toEpochMilli).toString}ms"),
      )){
        ZIO.logInfo("")
      }.as(response)
  })

  val httpApp =
    Routes(
      Method.GET / "api" / "games"              -> handler { (req: Request) =>
        val perPage = req.url.queryParams.get("perPage").flatMap(_.toIntOption).getOrElse(10)
        req.url.queryParams.get("page").flatMap(_.toIntOption) match
          case Some(page) =>
            ZIO
              .serviceWithZIO[ZIOGameService](_.list(page, perPage))
              .map(games => Response.json(games.toJson))
          case None       =>
            ZIO.succeed(Response.badRequest("page number not found"))
      },
      Method.POST / "api" / "games"             -> handler { (req: Request) =>
        for
          body: String       <- req.body.asString.orDie
          create: CreateGame <- ZIO
                                  .fromEither(body.fromJson[CreateGame])
                                  .mapError(msg => Response.badRequest(msg))
          game: Game         <- ZIO.serviceWithZIO[ZIOGameService](_.update(create.toGame))
        yield Response.json(game.toJson)
      },
      Method.GET / "api" / "games" / gameIdPath -> handler { (gameId: GameId, req: Request) =>
        ZIO
          .serviceWithZIO[ZIOGameService](_.get(gameId))
          .mapBoth(
            _ => Response.notFound(s"game with id $gameId not found"),
            game => Response.json(game.toJson),
          )
      },
    ).toHttpApp @@ requestLogging

  override def run: ZIO[Any, Any, Nothing] =
    Server
      .serve(httpApp)
      .provide(Server.default, gameRepoLayer, ZIOGameService.layer)
