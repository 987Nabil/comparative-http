package http

import domain.*
import repo.*

import scala.concurrent.*


class AkkaGameService(repo: GameRepo)(using ExecutionContext):

  def get(id: GameId): Future[Game] = Future(repo.get(id)).map {
    case Right(game) => game
    case Left(error) => throw error
  }

  def list(page: Int, perPage: Int): Future[Seq[Game]] =
    Future(repo.list.slice((page - 1) * perPage, page * perPage))

  def update(game: Game): Future[Game] = Future(repo.update(game))

  def delete(id: GameId): Future[Unit] = Future(repo.delete(id)).map {
    case Right(_)    => ()
    case Left(error) => throw error
  }
  
object AkkaGameService:
  def apply()(using ExecutionContext): AkkaGameService = new AkkaGameService(InMemoryGameRepo)
