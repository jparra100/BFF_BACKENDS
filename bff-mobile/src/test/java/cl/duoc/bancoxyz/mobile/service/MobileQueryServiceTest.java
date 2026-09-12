package cl.duoc.bancoxyz.mobile.service;

import cl.duoc.bancoxyz.mobile.exception.ResourceNotFoundException;
import cl.duoc.bancoxyz.mobile.repository.MobileDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MobileQueryServiceTest {

    private MobileQueryService service;

    @BeforeEach
    void setUp() {
        MobileDataRepository repository = new MobileDataRepository();
        repository.loadData();
        service = new MobileQueryService(repository);
    }

    @Test
    void returnsCompactAccountSummary() {
        var summary = service.getSummary(101);

        assertThat(summary.id()).isEqualTo(101);
        assertThat(summary.tipo()).isEqualTo("AHORRO");
        assertThat(summary.saldo()).isEqualByComparingTo(new BigDecimal("8000"));
    }

    @Test
    void limitsRecentMovements() {
        var movements = service.getRecentMovements(101, 3);

        assertThat(movements).hasSize(3);
        assertThat(movements.get(0).fecha()).isEqualTo(LocalDate.of(2024, 12, 22));
        assertThat(movements.get(0).tipo()).isEqualTo("DEPOSITO");
    }

    @Test
    void rejectsLimitOutsideMobileRange() {
        assertThatThrownBy(() -> service.getRecentMovements(101, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getRecentMovements(101, 11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reportsMissingAccount() {
        assertThatThrownBy(() -> service.getSummary(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
