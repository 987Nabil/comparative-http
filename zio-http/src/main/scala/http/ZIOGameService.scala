package http

import domain.*
import error.EntityNotFound
import repo.*
import zio.*


case class ZIOGameService(repo: GameRepo):

  def get(id: GameId): IO[EntityNotFound[GameId], Game] =
    ZIO.fromEither(repo.get(id))

  def list(page: Int, perPage: Int): UIO[Seq[Game]] =
    ZIO.succeed(repo.list.slice((page - 1) * perPage, page * perPage))

  def update(game: Game): UIO[Game] = ZIO.succeed(repo.update(game))

  def delete(id: GameId): IO[EntityNotFound[GameId], Unit] = ZIO.fromEither(repo.delete(id))
  
object ZIOGameService:
  val layer = ZLayer.derive[ZIOGameService]
