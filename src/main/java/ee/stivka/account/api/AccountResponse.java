package ee.stivka.account.api;

import ee.stivka.account.domain.Account;
import java.time.Instant;

public record AccountResponse(
    Long id,
    String name,
    String phoneNr,
    Instant createdDtime,
    Instant modifiedDtime) {

  public static AccountResponse from(Account account) {
    return new AccountResponse(
        account.getId(),
        account.getName(),
        account.getPhoneNr(),
        account.getCreatedDtime(),
        account.getModifiedDtime());
  }
}
