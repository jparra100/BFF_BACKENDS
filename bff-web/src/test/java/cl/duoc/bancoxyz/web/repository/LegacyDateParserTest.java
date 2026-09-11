package cl.duoc.bancoxyz.web.repository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LegacyDateParserTest {

    @Test
    void parsesAllLegacyFormats() {
        LocalDate expected = LocalDate.of(2024, 3, 18);

        assertEquals(expected, LegacyDateParser.parse("2024-03-18"));
        assertEquals(expected, LegacyDateParser.parse("2024/03/18"));
        assertEquals(expected, LegacyDateParser.parse("18-03-2024"));
        assertEquals(expected, LegacyDateParser.parse("18/03/2024"));
    }

    @Test
    void rejectsImpossibleDate() {
        assertThrows(IllegalArgumentException.class, () -> LegacyDateParser.parse("2024-13-01"));
    }
}

