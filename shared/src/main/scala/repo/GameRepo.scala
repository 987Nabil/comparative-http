package repo

import domain.*
import error.EntityNotFound

trait GameRepo:
  def get(id: GameId): Either[EntityNotFound[GameId], Game]
  def list: List[Game]
  def update(game: Game): Game
  def delete(id: GameId): Either[EntityNotFound[GameId], Unit]

object InMemoryGameRepo extends GameRepo:
  var repo: InMemoryRepo[GameId, Game] = InMemoryRepo.empty
  
  def get(id: GameId): Either[EntityNotFound[GameId], Game] =
    if repo.contains(id)
    then Right(repo.get(id).get)
    else Left(EntityNotFound(id))
    
  def list: List[Game] =
    repo.entities.values.toList

  def update(game: Game): Game =
    repo = repo.add(game.id, game)
    repo.get(game.id).get

  def delete(id: GameId): Either[EntityNotFound[GameId], Unit] =
    if !repo.contains(id)
    then Left(EntityNotFound(id))
    else
      repo = repo.delete(id)
      Right(())