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
    when(repository.saveAndFlush(any(Account.class))).thenReturn(stored);

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
    when(repository.saveAndFlush(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

    AccountResponse response = service.update(42L, new AccountRequest("Alicia", null));

    assertThat(response.name()).isEqualTo("Alicia");
    assertThat(response.phoneNr()).isNull();
    verify(repository).saveAndFlush(stored);
  }

  @Test
  void update_missingIdThrowsNotFound() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(99L, new AccountRequest("X", null)))
        .isInstanceOf(NotFoundException.class);
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void delete_invokesRepository() {
    when(repository.removeById(42L)).thenReturn(1);

    service.delete(42L);

    verify(repository).removeById(42L);
  }

  @Test
  void delete_missingIdThrowsNotFound() {
    when(repository.removeById(99L)).thenReturn(0);

    assertThatThrownBy(() -> service.delete(99L))
        .isInstanceOf(NotFoundException.class);
  }
}
