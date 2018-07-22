package com.krowd9.userexp.dto

import com.krowd9.api.addressbookdb.OffsetAndContact
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

  def apply(offsetAndContact: OffsetAndContact, userId: Option[Long]): Contact = Contact(
    offsetAndContact.contact.externalId,
    offsetAndContact.contact.name,
    userId
  )
}