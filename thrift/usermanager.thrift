/** User Manager Service */
namespace java com.krowd9.api.usermanager
#@namespace scala com.krowd9.api.usermanager

include "common.thrift"

/** Get Yakatak UserId Result */
struct GetYakatakUserIdResult {
  1: optional common.UserId userId;
}

/** Service that handles users */
service UserManagerService {
  /** Get Yakatak User ID for the ExternalId (if any) */
  GetYakatakUserIdResult getYakatakUserId(1: common.ExternalUserId externalId)
}
