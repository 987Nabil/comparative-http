package error

final case class EntityNotFound[EntityId](
    id: EntityId
  ) extends Exception(s"Entity with id $id not found")
