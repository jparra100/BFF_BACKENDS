package cl.duoc.bancoxyz.batch.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacyBatchProcessorsTest {

    private final LegacyBatchProcessors processors = new LegacyBatchProcessors();

    @Test
    void processesTheThreeLegacyFormats() {
        assertThat(processors.transaction(new String[]{"1", "2024-06-30", "3000", "credito"}).type())
                .isEqualTo("CREDITO");
        assertThat(processors.interest(new String[]{"137", "Bob Johnson", "7000", "30", "ahorro"}).monthlyInterest())
                .isEqualByComparingTo(new BigDecimal("17.50"));
        assertThat(processors.statement(new String[]{"103", "08-03-2024", "retiro", "1500", "Retiro parcial"}).amount())
                .isEqualByComparingTo(new BigDecimal("-1500"));
    }

    @Test
    void rejectsInvalidLegacyRows() {
        assertThatThrownBy(() -> processors.transaction(new String[]{"3", "2024-04-09", "800", "invalid"}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> processors.interest(new String[]{"114", "Unknown", "", "30", "-1"}))
                .isInstanceOf(RuntimeException.class);
    }
}
