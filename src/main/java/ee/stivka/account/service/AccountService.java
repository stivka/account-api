package ee.stivka.account.service;

import ee.stivka.account.api.AccountRequest;
import ee.stivka.account.api.AccountResponse;
import ee.stivka.account.domain.Account;
import ee.stivka.account.repository.AccountRepository;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class AccountService {

  private final AccountRepository repository;

  public AccountService(AccountRepository repository) {
    this.repository = repository;
  }

  public AccountResponse create(AccountRequest request) {
    Account account = new Account();
    account.setName(request.name());
    account.setPhoneNr(request.phoneNr());
    Account saved = repository.saveAndFlush(account);
    log.info("Created account {}", saved.getId());
    return AccountResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public AccountResponse get(Long id) {
    return AccountResponse.from(find(id));
  }

  public AccountResponse update(Long id, AccountRequest request) {
    Account account = find(id);
    account.setName(request.name());
    account.setPhoneNr(request.phoneNr());
    Account saved = repository.saveAndFlush(account);
    log.info("Updated account {}", saved.getId());
    return AccountResponse.from(saved);
  }

  public void delete(Long id) {
    if (repository.removeById(id) == 0) {
      throw new NotFoundException("Account %d not found".formatted(id));
    }
    log.info("Deleted account {}", id);
  }

  private Account find(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(id)));
  }

  @Transactional(readOnly = true)
  public List<String> searchByName(String query) {
    String normalizedQuery = query.trim().toLowerCase();

    return repository.findAll().stream()
        .map(Account::getName)
        .filter(name -> name.toLowerCase().contains(normalizedQuery))
        .sorted(Comparator.naturalOrder())
        .toList();
  }
}
