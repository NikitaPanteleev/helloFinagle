package com.krowd9.userexp

import com.krowd9.api.addressbookdb.{AddressBookDbService, GetContactsFilter}
import com.krowd9.api.usermanager.UserManagerService
import com.krowd9.userexp.dto.{Contact, Page}
import com.twitter.util.Future
import io.finch.syntax.get
import io.finch.{Endpoint, Ok, path}
import io.finch._

case class AddressBookApi(
  addressBookDb: AddressBookDbService.MethodPerEndpoint,
  userManager: UserManagerService.MethodPerEndpoint,
  config: Config
) {

  case class Params(
    limit: Int,
    offset: Option[String] = None,
    nextOffset: Option[String] = None,
    withId: Boolean,
  )

  def query(
    userId: Long,
    limit: Int,
    withId: Boolean,
    offset: Option[String] = None,
    contacts: Vector[Contact] = Vector.empty,
    map: Map[String, Option[Long]]
  ): Future[Page] = {
    addressBookDb
      .getContacts(userId, GetContactsFilter(offset))
      .flatMap(addresses => {
        val newIds = addresses.map(_.contact.externalId).toSet -- map.keySet
        val newMapping = newIds.toSeq.map(extId => userManager.getYakatakUserId(extId).map(intId => extId -> intId.userId))
        Future.collect(newMapping).map(mapping => (addresses, mapping))
      })
      .flatMap {
        case (addresses, newMapping) => {
          val updatedMapping = map ++ newMapping
          val newContacts = addresses
            .map(addr => Contact(addr, updatedMapping.get(addr.contact.externalId).flatten)).toVector
            .filter {
              case contact if withId  => contact.userId.nonEmpty
              case contact if !withId => true
            }

          val allFetchedContacts = if (contacts.lastOption == newContacts.headOption && contacts.lastOption.nonEmpty) {
            contacts ++ newContacts.tail
          } else {
            contacts ++ newContacts
          }

          if (allFetchedContacts.size > limit) {
            val nextContact = allFetchedContacts(limit)
            val nextOffset = addresses
              .find(addr => addr.contact.externalId == nextContact.externalId)
              .map(addr => addr.offset)
            Future.value(Page(
              data = allFetchedContacts.take(limit),
              nextOffset = nextOffset
            ))
          } else if (allFetchedContacts.size <= limit && addresses.size < config.dbPageSize) {
            Future.value(Page(
              data = allFetchedContacts,
              nextOffset = None
            ))
          } else {
            query(
              userId = userId,
              limit = limit,
              withId = withId,
              offset = addresses.lastOption.map(addr => addr.offset),
              allFetchedContacts,
              updatedMapping
            )
          }
        }
      }
  }

  val params = (
    paramOption[Int]("limit").withDefault(config.defaultLimit)
      :: paramOption[String]("offset")
      :: paramOption[String]("nextOffset")
      :: paramOption[Boolean]("withId").withDefault(false)
    ).as[Params]

  val api: Endpoint[Page] = get("addressBook" :: path[Long] :: params) { (userId: Long, p: Params) =>

    query(
      userId = userId,
      limit = math.min(p.limit, config.contactsLimit),
      withId = p.withId,
      offset = p.offset,
      Vector.empty,
      Map.empty
    ).map(Ok)
  }
}
