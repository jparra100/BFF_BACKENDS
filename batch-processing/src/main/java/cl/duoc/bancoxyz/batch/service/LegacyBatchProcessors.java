package cl.duoc.bancoxyz.batch.service;

import cl.duoc.bancoxyz.batch.model.AnnualStatementLine;
import cl.duoc.bancoxyz.batch.model.InterestResult;
import cl.duoc.bancoxyz.batch.model.ProcessedTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class LegacyBatchProcessors {

    private static final Set<String> TRANSACTION_TYPES = Set.of("CREDITO", "DEBITO");
    private static final Set<String> ACCOUNT_TYPES = Set.of("AHORRO", "PRESTAMO", "HIPOTECA");
    private static final Set<String> MOVEMENT_TYPES = Set.of("DEPOSITO", "RETIRO", "COMPRA", "PAGO");
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-uuuu"),
            DateTimeFormatter.ofPattern("dd/MM/uuuu"),
            DateTimeFormatter.ofPattern("uuuu/MM/dd")
    );

    public ProcessedTransaction transaction(String[] values) {
        requireColumns(values, 4);
        long id = positiveLong(values[0]);
        LocalDate date = date(values[1]);
        BigDecimal amount = positiveAmount(values[2]);
        String type = allowed(values[3], TRANSACTION_TYPES);
        return new ProcessedTransaction(id, date, amount, type);
    }

    public InterestResult interest(String[] values) {
        requireColumns(values, 5);
        long accountId = positiveLong(values[0]);
        String holder = values[1].trim();
        BigDecimal balance = new BigDecimal(values[2].trim());
        int age = Integer.parseInt(values[3].trim());
        String type = allowed(values[4], ACCOUNT_TYPES);
        if (holder.isBlank() || holder.equalsIgnoreCase("Unknown") || balance.signum() < 0 || age < 18 || age > 100) {
            throw new IllegalArgumentException("Cuenta inválida");
        }
        BigDecimal annualRate = switch (type) {
            case "AHORRO" -> new BigDecimal("0.03");
            case "PRESTAMO" -> new BigDecimal("0.12");
            default -> new BigDecimal("0.05");
        };
        BigDecimal monthly = balance.multiply(annualRate)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        return new InterestResult(accountId, holder, type, balance, monthly);
    }

    public AnnualStatementLine statement(String[] values) {
        requireColumns(values, 5);
        long accountId = positiveLong(values[0]);
        LocalDate date = date(values[1]);
        String type = allowed(values[2], MOVEMENT_TYPES);
        BigDecimal amount = new BigDecimal(values[3].trim());
        if (amount.signum() == 0) {
            throw new IllegalArgumentException("Movimiento sin monto");
        }
        if (!"DEPOSITO".equals(type)) {
            amount = amount.abs().negate();
        }
        String description = values[4].trim().isEmpty() ? "Sin descripción" : values[4].trim();
        return new AnnualStatementLine(accountId, date, type, amount, description);
    }

    private void requireColumns(String[] values, int expected) {
        if (values == null || values.length != expected) {
            throw new IllegalArgumentException("Cantidad de columnas inválida");
        }
    }

    private long positiveLong(String value) {
        long parsed = Long.parseLong(value.trim());
        if (parsed <= 0) {
            throw new IllegalArgumentException("Identificador inválido");
        }
        return parsed;
    }

    private BigDecimal positiveAmount(String value) {
        BigDecimal amount = new BigDecimal(value.trim());
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Monto inválido");
        }
        return amount;
    }

    private String allowed(String value, Set<String> allowed) {
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException("Tipo inválido");
        }
        return normalized;
    }

    private LocalDate date(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // Se intenta el siguiente formato legacy.
            }
        }
        throw new IllegalArgumentException("Fecha inválida");
    }
}
