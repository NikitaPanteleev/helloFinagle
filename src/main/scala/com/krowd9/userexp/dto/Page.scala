package com.krowd9.userexp.dto

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Page(
  data: Vector[Contact],
  nextOffset: Option[String]
) {
  def map(f: Contact => Contact) = this.copy(data = data.map(f))
}

object Page {
  implicit val decoder: Decoder[Page] = deriveDecoder[Page]
  implicit val encoder: Encoder[Page] = deriveEncoder[Page]
}