package ee.stivka.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ee.stivka.account.api.AccountRequest;
import ee.stivka.account.api.AccountResponse;
import ee.stivka.account.domain.Account;
import ee.stivka.account.repository.AccountRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock
  private AccountRepository repository;

  @InjectMocks
  private AccountService service;

  private Account stored;

  @BeforeEach
  void setUp() {
    stored = new Account();
    stored.setId(42L);
    stored.setName("Alice");
    stored.setPhoneNr("+3725551234");
    stored.setCreatedDtime(Instant.parse("2026-04-17T10:00:00Z"));
    stored.setModifiedDtime(Instant.parse("2026-04-17T10:00:00Z"));
  }

  @Test
  void create_persistsAndReturnsResponse() {
    when(repository.save(any(Account.class))).thenReturn(stored);

    AccountResponse response = service.create(new AccountRequest("Alice", "+3725551234"));

    assertThat(response.id()).isEqualTo(42L);
    assertThat(response.name()).isEqualTo("Alice");
    assertThat(response.phoneNr()).isEqualTo("+3725551234");
    assertThat(response.createdDtime()).isEqualTo(Instant.parse("2026-04-17T10:00:00Z"));
  }

  @Test
  void get_returnsMappedResponse() {
    when(repository.findById(42L)).thenReturn(Optional.of(stored));

    AccountResponse response = service.get(42L);

    assertThat(response.name()).isEqualTo("Alice");
  }

  @Test
  void get_missingIdThrowsNotFound() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(99L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  void update_mutatesAndSaves() {
    when(repository.findById(42L)).thenReturn(Optional.of(stored));
    when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

    AccountResponse response = service.update(42L, new AccountRequest("Alicia", null));

    assertThat(response.name()).isEqualTo("Alicia");
    assertThat(response.phoneNr()).isNull();
    verify(repository).save(stored);
  }

  @Test
  void update_missingIdThrowsNotFound() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(99L, new AccountRequest("X", null)))
        .isInstanceOf(NotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void delete_invokesRepository() {
    when(repository.existsById(42L)).thenReturn(true);

    service.delete(42L);

    verify(repository).deleteById(42L);
  }

  @Test
  void delete_missingIdThrowsNotFound() {
    when(repository.existsById(99L)).thenReturn(false);

    assertThatThrownBy(() -> service.delete(99L))
        .isInstanceOf(NotFoundException.class);
    verify(repository, never()).deleteById(any());
  }
}
