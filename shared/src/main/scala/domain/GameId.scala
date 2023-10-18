package domain

import java.util.UUID

opaque type GameId = UUID

object GameId:
  def apply(value:   UUID): GameId         = value
  def unapply(value: GameId): Option[UUID] = Some(value)
  def generate: GameId = java.util.UUID.randomUUID()
