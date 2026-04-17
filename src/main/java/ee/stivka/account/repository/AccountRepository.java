package ee.stivka.account.repository;

import ee.stivka.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

  @Modifying
  @Query("delete from Account a where a.id = :id")
  int removeById(@Param("id") Long id);
}
