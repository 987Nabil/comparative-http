package domain

import java.util.UUID

opaque type ReviewId = UUID

object ReviewId:
  def apply(id: UUID): ReviewId   = id
  def apply(id: String): ReviewId = UUID.fromString(id)
  def generate: ReviewId = UUID.randomUUID()
