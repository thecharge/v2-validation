package hl7.v2.instance

import hl7.v2.profile.{Message => MM}

/**
  * Class representing a message
  */
case class Message(
    model: MM,
    children: List[SegOrGroup],
    invalid: List[(Int, String)],
    unexpected: List[(Int, String)],
    defaultTimeZone: Option[TimeZone],//FIXME: Get this from MSH.7 ?
    separators: Separators
) {

  lazy val asGroup = Group( model.asGroup, 1, children)

  lazy val location = asGroup.location

  val tree = serializer.Tree.message(this)
}
