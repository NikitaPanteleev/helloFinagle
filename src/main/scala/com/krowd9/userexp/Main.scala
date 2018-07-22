package com.krowd9.userexp

import com.krowd9.api.addressbookdb.{AddressBookDbService, GetContactsFilter, OffsetAndContact}
import com.krowd9.api.common.UserNotFoundException
import com.krowd9.api.usermanager.{GetYakatakUserIdResult, UserManagerService}
import com.twitter.finagle.{Http, Service, Thrift, ThriftMux}
import com.twitter.util.Future
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.syntax._
import io.finch._
import io.finch.circe._
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto._

object Main extends App {
  case class Contact(
    externalId: String,
    name: Option[String],
    userId: Option[Long]
  )

  object Contact {
    implicit val decoder: Decoder[Contact] = deriveDecoder[Contact]
    implicit val encoder: Encoder[Contact] = deriveEncoder[Contact]
  }

  case class Page(msg: String)

  val addressBookDb: AddressBookDbService.MethodPerEndpoint =
    Thrift.client.build[AddressBookDbService.MethodPerEndpoint]("127.0.0.1:7201")
  val userManager: UserManagerService.MethodPerEndpoint =
    Thrift.client.build[UserManagerService.MethodPerEndpoint]("127.0.0.1:7202")

  val address: Endpoint[Seq[Contact]] = get("addressBook" :: path[Long]) { userId: Long =>
    val result = for {
      addresses <- addressBookDb.getContacts(userId, GetContactsFilter())
      externalIds <- {
        val externalIdsF: Seq[Future[(String, GetYakatakUserIdResult)]] = addresses
          .map(_.contact.externalId)
          .distinct
          .map(externalId => userManager.getYakatakUserId(externalId).map(res => externalId -> res))
        Future.collect(externalIdsF).map(_.toMap)
      }
    } yield {
      addresses.map(contact => Contact(
        contact.contact.externalId,
        contact.contact.name,
        externalIds.get(contact._2.externalId).flatMap(res => res.userId))
      )
    }

    result.map(Ok)
  }


  val api: Service[Request, Response] = address
    .handle({
      case e: UserNotFoundException => NotFound(e)
    }).toServiceAs[Application.Json]

  Await.ready(Http.server.serve(":8080", api))
}