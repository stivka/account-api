package ee.stivka.account.api;

import java.time.Instant;

public record AccountResponse(
    Long id,
    String name,
    String phoneNr,
    Instant createdDtime,
    Instant modifiedDtime) {
}
