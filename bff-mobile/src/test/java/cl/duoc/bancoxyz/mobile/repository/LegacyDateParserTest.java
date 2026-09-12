package cl.duoc.bancoxyz.mobile.repository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacyDateParserTest {

    @Test
    void acceptsKnownLegacyFormats() {
        assertThat(LegacyDateParser.parse("2024-12-22")).isEqualTo(LocalDate.of(2024, 12, 22));
        assertThat(LegacyDateParser.parse("22/12/2024")).isEqualTo(LocalDate.of(2024, 12, 22));
    }

    @Test
    void rejectsInvalidDate() {
        assertThatThrownBy(() -> LegacyDateParser.parse("31/02/2024"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
