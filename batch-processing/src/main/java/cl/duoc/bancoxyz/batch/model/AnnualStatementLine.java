package cl.duoc.bancoxyz.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnnualStatementLine(long accountId, LocalDate date, String type,
                                  BigDecimal amount, String description) {
}
