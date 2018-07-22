package com.krowd9.userexp.dto

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Page(
  msg: String,
  data: Seq[Contact]
)

object Page {
  implicit val decoder: Decoder[Page] = deriveDecoder[Page]
  implicit val encoder: Encoder[Page] = deriveEncoder[Page]
}