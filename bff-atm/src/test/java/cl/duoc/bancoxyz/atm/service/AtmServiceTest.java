package cl.duoc.bancoxyz.atm.service;

import cl.duoc.bancoxyz.atm.exception.AccountNotFoundException;
import cl.duoc.bancoxyz.atm.exception.InsufficientFundsException;
import cl.duoc.bancoxyz.atm.repository.AtmAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtmServiceTest {

    private AtmService service;

    @BeforeEach
    void setUp() {
        AtmAccountRepository repository = new AtmAccountRepository();
        repository.loadAccounts();
        service = new AtmService(repository);
    }

    @Test
    void returnsAvailableBalance() {
        assertThat(service.getBalance(101).saldoDisponible())
                .isEqualByComparingTo(new BigDecimal("8000"));
    }

    @Test
    void withdrawsAndUpdatesBalance() {
        var result = service.withdraw(101, new BigDecimal("1000"));

        assertThat(result.montoRetirado()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(result.saldoDisponible()).isEqualByComparingTo(new BigDecimal("7000"));
        assertThat(service.getBalance(101).saldoDisponible()).isEqualByComparingTo(new BigDecimal("7000"));
    }

    @Test
    void leavesBalanceUnchangedWhenFundsAreInsufficient() {
        assertThatThrownBy(() -> service.withdraw(101, new BigDecimal("8001")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(service.getBalance(101).saldoDisponible()).isEqualByComparingTo(new BigDecimal("8000"));
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatThrownBy(() -> service.withdraw(101, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El monto debe ser mayor que cero");
    }

    @Test
    void reportsMissingAccount() {
        assertThatThrownBy(() -> service.getBalance(999))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
