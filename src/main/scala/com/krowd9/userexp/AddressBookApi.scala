package com.krowd9.userexp

import com.krowd9.api.addressbookdb.{AddressBookDbService, GetContactsFilter}
import com.krowd9.api.usermanager.{GetYakatakUserIdResult, UserManagerService}
import com.krowd9.userexp.dto.{Contact, Page}
import com.twitter.util.Future
import io.finch.syntax.get
import io.finch.{Endpoint, Ok, path}

case class AddressBookApi(
  addressBookDb: AddressBookDbService.MethodPerEndpoint,
  userManager: UserManagerService.MethodPerEndpoint
) {
  val api: Endpoint[Page] = get("addressBook" :: path[Long]) { userId: Long =>
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
      val data = addresses.map(contact => Contact(
        contact.contact.externalId,
        contact.contact.name,
        externalIds.get(contact._2.externalId).flatMap(res => res.userId))
      )
      Page("example", data)
    }

    result.map(Ok)
  }
}
