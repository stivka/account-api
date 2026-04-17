package ee.stivka.account.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountRequest(
    @NotBlank String name,
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "phoneNr must be a valid phone number") String phoneNr) {
}
