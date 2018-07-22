package com.krowd9.userexp

import com.krowd9.api.addressbookdb.{AddressBookContact, AddressBookDbService, GetContactsFilter, OffsetAndContact}
import com.krowd9.api.usermanager.{GetYakatakUserIdResult, UserManagerService}
import com.krowd9.userexp.dto.Page
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, fixture}
import io.finch._
import io.finch.test.ServiceSuite
import io.finch.circe._
import org.mockito.{ArgumentMatchers => m}
import org.mockito.Mockito.when
import io.circe.syntax._
import io.circe.parser._

class AddressBookApiTest extends fixture.FlatSpecLike
  with ServiceSuite
  with Matchers
  with MockitoSugar {

  val address: AddressBookDbService.MethodPerEndpoint = mock[AddressBookDbService.MethodPerEndpoint]

  val userManager: UserManagerService.MethodPerEndpoint = mock[UserManagerService.MethodPerEndpoint]

  def createService(): Service[Request, Response] = AddressBookApi(address, userManager)
    .api
    .toServiceAs[Application.Json]

  it should "return 0 contacts list if no contacts" in { f =>
    when(address.getContacts(m.eq(1L), m.any[GetContactsFilter])).thenReturn(Future.value(Nil))
    val res = f(Request("/addressBook/1"))
    res.status should be(Status.Ok)
  }

  it should "return contacts list if there are some contacts" in { f =>
    val contacts = (1 to 10).map(i => OffsetAndContact.apply(
      s"offset_$i",
      AddressBookContact.apply(s"ext$i", Some(s"name_$i"))
    ))
    when(address.getContacts(m.eq(1L), m.any[GetContactsFilter])).thenReturn(Future.value(contacts))
    when(userManager.getYakatakUserId(m.any[String])).thenReturn(Future.value(GetYakatakUserIdResult.apply(None)))

    val res = f(Request("/addressBook/1"))

    res.status should be(Status.Ok)

    val pageJson = parse(res.contentString).flatMap(_.as[Page])
    pageJson.isRight shouldBe true
    pageJson.right.get.data.map(_.externalId) should contain theSameElementsAs contacts.map(_.contact.externalId)
    pageJson.right.get.data.map(_.name) should contain theSameElementsAs contacts.map(_.contact.name)

  }
}
