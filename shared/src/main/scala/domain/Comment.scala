package domain

opaque type Comment = String

object Comment:
  def apply(comment:   String): Comment         = comment
  def unapply(comment: Comment): Option[String] = Some(comment)
