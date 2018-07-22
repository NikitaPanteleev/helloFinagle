/** The Address Book DB Service manages user contacts DB */
namespace java com.krowd9.api.addressbookdb
#@namespace scala com.krowd9.api.addressbookdb

include "common.thrift"

/** Contact Offset */
typedef string Offset

/** Contact object */
struct AddressBookContact {
  /** External User ID */
  1: required common.ExternalUserId externalId;
  /** Name */
  2: optional string name;
}

/** Offset and Contact pair */
struct OffsetAndContact {
  /** Contact Offset (to scan DB) */
  1: required Offset offset;
  /** Address Book Contact */
  2: required AddressBookContact contact;
}

/** Get Contacts Filter */
struct GetContactsFilter {
  /** Contact offset */
  1: optional Offset offset;
}

/** Address Book DB Service */
service AddressBookDbService {
  /** Returns at most 10 contacts from the DB from userId's Address Book starting at the offset (if not set starts from the beginning) */
  list<OffsetAndContact> getContacts(1: common.UserId userId, 2: GetContactsFilter filter)
    throws (1: common.UserNotFoundException notFound)
}