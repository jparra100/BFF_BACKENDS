package cl.duoc.bancoxyz.accounts.repository;

import cl.duoc.bancoxyz.accounts.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
}
