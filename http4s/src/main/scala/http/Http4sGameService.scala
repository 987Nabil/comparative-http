package http

import cats.effect.kernel.Async
import domain.*
import repo.*

import scala.concurrent.*


class Http4sGameService[F[_]: Async](repo: GameRepo):

  def get(id: GameId): F[Game] = Async[F].fromEither(repo.get(id))

  def list(page: Int, perPage: Int): F[Seq[Game]] =
    Async[F].pure(repo.list.slice((page - 1) * perPage, page * perPage))

  def update(game: Game): F[Game] = Async[F].pure(repo.update(game))

  def delete(id: GameId): F[Unit] = Async[F].fromEither(repo.delete(id))
  
object Http4sGameService:
  def apply[F[_]: Async]: Http4sGameService[F] = new Http4sGameService(InMemoryGameRepo)
