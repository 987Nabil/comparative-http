package domain

opaque type Publisher = String

object Publisher:
  def apply(publisher: String): Publisher = publisher
  def unapply(publisher: Publisher): Option[String] = Some(publisher)