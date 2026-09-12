package cl.duoc.bancoxyz.mobile.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MobileDataRepositoryTest {

    private MobileDataRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MobileDataRepository();
        repository.loadData();
    }

    @Test
    void loadsAccountFieldsRequiredByMobile() {
        var account = repository.findAccount(101).orElseThrow();

        assertThat(repository.accountCount()).isEqualTo(50);
        assertThat(account.type()).isEqualTo("AHORRO");
        assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("8000"));
    }

    @Test
    void ordersMovementsFromNewestToOldest() {
        var movements = repository.findMovements(101);

        assertThat(movements).isNotEmpty();
        assertThat(movements).isSortedAccordingTo(
                (left, right) -> right.date().compareTo(left.date()));
    }
}
