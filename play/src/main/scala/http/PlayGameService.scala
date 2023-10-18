package http

import com.google.inject.*
import domain.*
import repo.*

import scala.concurrent.*

@Singleton
class GameRepoProvider@Inject()() extends Provider[GameRepo]:
  def get(): GameRepo = InMemoryGameRepo

@Singleton
class PlayGameService @Inject() (repo: GameRepo)(using ExecutionContext):

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
