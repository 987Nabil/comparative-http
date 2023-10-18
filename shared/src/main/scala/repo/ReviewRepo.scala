package repo

import error.EntityNotFound
import domain.*

trait ReviewRepo {
  def get(id: ReviewId): Either[EntityNotFound[ReviewId], Review]
  def update(review: Review): Review
  def delete(id: ReviewId): Either[EntityNotFound[ReviewId], Unit]
}

object InMemoryReviewRepo extends ReviewRepo {
  var repo: InMemoryRepo[ReviewId, Review] = InMemoryRepo.empty
  def get(id: ReviewId): Either[EntityNotFound[ReviewId], Review] =
    if repo.contains(id)
    then Right(repo.get(id).get)
    else Left(EntityNotFound(id))

  def update(review: Review): Review =
    repo = repo.add(review.id, review)
    repo.get(review.id).get

  def delete(id: ReviewId): Either[EntityNotFound[ReviewId], Unit] =
    if !repo.contains(id)
    then Left(EntityNotFound(id))
    else
      repo = repo.delete(id)
      Right(())
}