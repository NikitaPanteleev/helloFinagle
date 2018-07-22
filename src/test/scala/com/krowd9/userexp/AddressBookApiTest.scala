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
import io.circe.parser._

class AddressBookApiTest extends fixture.FlatSpecLike
  with ServiceSuite
  with Matchers
  with MockitoSugar {

  val address: AddressBookDbService.MethodPerEndpoint = mock[AddressBookDbService.MethodPerEndpoint]

  val userManager: UserManagerService.MethodPerEndpoint = mock[UserManagerService.MethodPerEndpoint]

  val contacts = (0 to 250).map(i => OffsetAndContact.apply(
    i.toString,
    AddressBookContact.apply(s"ext$i", Some(s"name_$i"))
  ))

  when(address.getContacts(1L, GetContactsFilter(None))).thenReturn(Future.value(contacts.take(10)))
  (0 to 250).foreach { offset =>
    when(address.getContacts(1L, GetContactsFilter(Some(offset.toString))))
      .thenReturn(Future.value(contacts.slice(offset, offset + 10)))
    when(userManager.getYakatakUserId(s"ext$offset")).thenReturn(Future.value(GetYakatakUserIdResult.apply(Some(offset))))
  }
  when(address.getContacts(m.eq(0L), GetContactsFilter(m.any[Option[String]]))).thenReturn(Future.value(Nil))

  def createService(): Service[Request, Response] = AddressBookApi(address, userManager, Config())
    .api
    .toServiceAs[Application.Json]

  it should "return 0 contacts list if no contacts" in { f =>
    val res = f(Request("/addressBook/0"))
    res.status should be(Status.Ok)
  }

  it should "return contacts with internal user ids" in { f =>
   val res = f(Request("/addressBook/1"))

    res.status should be(Status.Ok)

    val pageJson = parse(res.contentString).flatMap(_.as[Page])
    pageJson.isRight shouldBe true
    pageJson.right.get.data.map(_.externalId) should contain theSameElementsAs contacts.take(25).map(_.contact.externalId)
    pageJson.right.get.data.map(_.name) should contain theSameElementsAs contacts.take(25).map(_.contact.name)
    pageJson.right.get.nextOffset shouldBe Some("25")
    pageJson.right.get.data.foreach{contact =>
      contact.userId should not be empty
      contact.name should be (Some(s"name_${contact.userId.get}"))
      contact.externalId should be (s"ext${contact.userId.get}")
    }

  }

  it should "support pagination" in { f =>

    val res = f(Request("/addressBook/1?offset=10&limit=24"))

    res.status should be(Status.Ok)

    val pageJson = parse(res.contentString).flatMap(_.as[Page])
    pageJson.isRight shouldBe true
    pageJson.right.get.data.map(_.externalId) should contain theSameElementsAs (10 to 33).map(i => s"ext$i")
    pageJson.right.get.nextOffset should be (Some("34"))
  }

  it should "support pagination in the end of data" in { f =>

    val res = f(Request("/addressBook/1?offset=230&limit=50"))

    res.status should be(Status.Ok)

    val pageJson = parse(res.contentString).flatMap(_.as[Page])
    pageJson.isRight shouldBe true
    pageJson.right.get.data.map(_.externalId) should contain theSameElementsAs (230 to 250).map(i => s"ext$i")
    pageJson.right.get.nextOffset should be (None)
  }
}
