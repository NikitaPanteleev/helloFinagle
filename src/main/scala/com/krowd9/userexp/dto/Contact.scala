package com.krowd9.userexp.dto

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Contact(
  externalId: String,
  name: Option[String],
  userId: Option[Long]
)

object Contact {
  implicit val decoder: Decoder[Contact] = deriveDecoder[Contact]
  implicit val encoder: Encoder[Contact] = deriveEncoder[Contact]
}