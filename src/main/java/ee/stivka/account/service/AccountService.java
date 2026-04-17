package ee.stivka.account.service;

import ee.stivka.account.api.AccountRequest;
import ee.stivka.account.api.AccountResponse;
import ee.stivka.account.domain.Account;
import ee.stivka.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    return toResponse(repository.save(account));
  }

  @Transactional(readOnly = true)
  public AccountResponse get(Long id) {
    return toResponse(find(id));
  }

  public AccountResponse update(Long id, AccountRequest request) {
    Account account = find(id);
    account.setName(request.name());
    account.setPhoneNr(request.phoneNr());
    return toResponse(repository.save(account));
  }

  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("Account %d not found".formatted(id));
    }
    repository.deleteById(id);
  }

  private Account find(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(id)));
  }

  private static AccountResponse toResponse(Account account) {
    return new AccountResponse(
        account.getId(),
        account.getName(),
        account.getPhoneNr(),
        account.getCreatedDtime(),
        account.getModifiedDtime());
  }
}
