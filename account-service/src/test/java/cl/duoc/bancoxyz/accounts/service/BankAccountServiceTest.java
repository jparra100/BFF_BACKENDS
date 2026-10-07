package cl.duoc.bancoxyz.accounts.service;

import cl.duoc.bancoxyz.accounts.dto.AccountRequest;
import cl.duoc.bancoxyz.accounts.model.BankAccount;
import cl.duoc.bancoxyz.accounts.repository.BankAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BankAccountServiceTest {
    @Test void opensAndDebitsAnAccount() {
        BankAccountRepository repository = mock(BankAccountRepository.class);
        BankAccount account = new BankAccount(7L, "AHORRO", new BigDecimal("1000"));
        when(repository.save(any())).thenReturn(account);
        when(repository.findById(1L)).thenReturn(Optional.of(account));
        BankAccountService service = new BankAccountService(repository);
        assertThat(service.open(new AccountRequest(7L, "ahorro", new BigDecimal("1000"))).type()).isEqualTo("AHORRO");
        assertThat(service.debit(1L, new BigDecimal("250")).balance()).isEqualByComparingTo("750");
    }
}
