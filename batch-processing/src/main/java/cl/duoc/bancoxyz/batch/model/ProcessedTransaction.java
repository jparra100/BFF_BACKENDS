package cl.duoc.bancoxyz.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProcessedTransaction(long id, LocalDate date, BigDecimal amount, String type) {
}
