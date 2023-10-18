package domain

import java.time.Instant

final case class Game(
    id: GameId,
    name: String,
    platforms: List[Platform],
    publisher: Publisher,
    developer: Developer,
    releaseDate: Instant,
    updatedAt: Instant,
  )
