package cl.duoc.bancoxyz.atm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AtmAccountRepositoryTest {

    private AtmAccountRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AtmAccountRepository();
        repository.loadAccounts();
    }

    @Test
    void loadsFirstValidBalanceForEveryAccount() {
        assertThat(repository.count()).isEqualTo(50);
        assertThat(repository.findById(101).orElseThrow().balance())
                .isEqualByComparingTo(new BigDecimal("8000"));
    }

    @Test
    void ignoresFieldsThatTheAtmDoesNotNeed() {
        assertThat(repository.findById(110).orElseThrow().balance())
                .isEqualByComparingTo(new BigDecimal("5000"));
    }
}
