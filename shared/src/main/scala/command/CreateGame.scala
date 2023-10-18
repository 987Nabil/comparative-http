package command

import domain.*

import java.time.Instant

case class CreateGame(
    name: String,
    platforms: List[Platform],
    publisher: Publisher,
    developer: Developer,
    releaseDate: Instant,
  ):

  def toGame: Game =
    Game(
      GameId.generate,
      name,
      platforms,
      publisher,
      developer,
      releaseDate,
      Instant.now,
    )
