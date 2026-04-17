package ee.stivka.account.api;

import ee.stivka.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accounts", description = "CRUD operations for account management")
@RestController
@RequestMapping("/accounts")
public class AccountController {

  private final AccountService service;

  public AccountController(AccountService service) {
    this.service = service;
  }

  @Operation(summary = "Create a new account")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Account created"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "409", description = "Phone number already in use")
  })
  @PostMapping
  public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
    AccountResponse response = service.create(request);
    return ResponseEntity.created(URI.create("/accounts/" + response.id())).body(response);
  }

  @Operation(summary = "Retrieve an account by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Account found"),
      @ApiResponse(responseCode = "404", description = "Account not found")
  })
  @GetMapping("/{id}")
  public AccountResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @Operation(summary = "Full update of an account (PUT replaces all fields)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Account updated"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "404", description = "Account not found"),
      @ApiResponse(responseCode = "409", description = "Phone number already in use")
  })
  @PutMapping("/{id}")
  public AccountResponse update(@PathVariable Long id, @Valid @RequestBody AccountRequest request) {
    return service.update(id, request);
  }

  @Operation(summary = "Delete an account by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Account deleted"),
      @ApiResponse(responseCode = "404", description = "Account not found")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
