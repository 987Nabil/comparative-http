package repo

final case class InMemoryRepo[Id, Entity](entities: Map[Id, Entity]):

  def get(id: Id): Option[Entity] =
    entities.get(id)

  def add(id: Id, entity: Entity): InMemoryRepo[Id, Entity] =
    InMemoryRepo(entities + (id -> entity))

  def delete(id: Id): InMemoryRepo[Id, Entity] =
    InMemoryRepo(entities - id)

  def contains(id: Id): Boolean =
    entities.contains(id)

object InMemoryRepo:
  def empty[Id, Entity]: InMemoryRepo[Id, Entity] =
    InMemoryRepo(Map.empty)