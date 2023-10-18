package domain

opaque type Developer = String

object Developer:
  def apply(value:       String): Developer         = value
  def unapply(developer: Developer): Option[String] = Some(developer)
