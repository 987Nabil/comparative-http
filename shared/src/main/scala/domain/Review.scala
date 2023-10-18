package domain

final case class Review(
    id: ReviewId,
    gameId: GameId,
    rating: Rating,
    comment: Option[Comment],
  )

object Review:

  def apply(gameId: GameId, rating: Rating, comment: Option[Comment]): Review =
    Review(
      id = ReviewId.generate,
      gameId = gameId,
      rating = rating,
      comment = comment,
    )
