package com.krowd9.userexp

import com.krowd9.api.addressbookdb.{AddressBookDbService, GetContactsFilter, OffsetAndContact}
import com.krowd9.api.common.UserNotFoundException
import com.krowd9.api.usermanager.{GetYakatakUserIdResult, UserManagerService}
import com.krowd9.userexp.dto.Contact
import com.twitter.finagle.{Http, Service, Thrift, ThriftMux}
import com.twitter.util.Future
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.syntax._
import io.finch._
import io.finch.circe._


object Main extends App {
  val config = Config()
  val addressBookDb: AddressBookDbService.MethodPerEndpoint =
    Thrift.client.build[AddressBookDbService.MethodPerEndpoint]("127.0.0.1:7201")
  val userManager: UserManagerService.MethodPerEndpoint =
    Thrift.client.build[UserManagerService.MethodPerEndpoint]("127.0.0.1:7202")

  val addressBookApi = AddressBookApi(addressBookDb, userManager, config)

  val api: Service[Request, Response] = addressBookApi.api
    .handle({
      case e: UserNotFoundException => NotFound(e)
    }).toServiceAs[Application.Json]

  Await.ready(Http.server.serve(":8080", api))
}