package cl.duoc.bancoxyz.web.repository;

import cl.duoc.bancoxyz.web.model.Account;
import cl.duoc.bancoxyz.web.model.AnnualMovement;
import cl.duoc.bancoxyz.web.model.LegacyTransaction;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class WebDataRepository {

    private static final Logger log = LoggerFactory.getLogger(WebDataRepository.class);
    private static final Set<String> ACCOUNT_TYPES = Set.of("AHORRO", "PRESTAMO", "HIPOTECA");
    private static final Set<String> MOVEMENT_TYPES = Set.of("DEPOSITO", "RETIRO", "COMPRA", "PAGO");
    private static final Set<String> TRANSACTION_TYPES = Set.of("CREDITO", "DEBITO");

    private final Map<Long, Account> accounts = new LinkedHashMap<>();
    private final List<AnnualMovement> movements = new ArrayList<>();
    private final List<LegacyTransaction> transactions = new ArrayList<>();

    @PostConstruct
    public void loadData() {
        loadAccounts();
        loadMovements();
        loadTransactions();
    }

    public List<Account> findAllAccounts() {
        return accounts.values().stream()
                .sorted(Comparator.comparingLong(Account::accountId))
                .toList();
    }

    public Optional<Account> findAccount(long accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public List<AnnualMovement> findMovements(long accountId) {
        return movements.stream()
                .filter(movement -> movement.accountId() == accountId)
                .sorted(Comparator.comparing(AnnualMovement::date).reversed())
                .toList();
    }

    public List<LegacyTransaction> findAllTransactions() {
        return transactions.stream()
                .sorted(Comparator.comparing(LegacyTransaction::date).reversed()
                        .thenComparing(Comparator.comparingLong(LegacyTransaction::id).reversed()))
                .toList();
    }

    private void loadAccounts() {
        int read = 0;
        int discarded = 0;
        for (String line : readLines("data/intereses.csv")) {
            read++;
            try {
                String[] values = split(line, 5);
                long id = Long.parseLong(values[0]);
                String name = values[1];
                BigDecimal balance = new BigDecimal(values[2]);
                int age = Integer.parseInt(values[3]);
                String type = normalize(values[4]);

                if (id <= 0 || name.isBlank() || name.equalsIgnoreCase("Unknown")
                        || balance.signum() < 0 || age < 18 || age > 100 || !ACCOUNT_TYPES.contains(type)) {
                    throw new IllegalArgumentException("Cuenta incompleta");
                }

                if (accounts.putIfAbsent(id, new Account(id, name, balance, age, type)) != null) {
                    discarded++;
                }
            } catch (RuntimeException ex) {
                discarded++;
            }
        }
        log.info("Cuentas web cargadas: {}, descartadas: {}, leídas: {}", accounts.size(), discarded, read);
    }

    private void loadMovements() {
        int read = 0;
        int discarded = 0;
        Set<AnnualMovement> unique = new LinkedHashSet<>();
        for (String line : readLines("data/cuentas_anuales.csv")) {
            read++;
            try {
                String[] values = split(line, 5);
                long accountId = Long.parseLong(values[0]);
                String type = normalize(values[2]);
                BigDecimal amount = normalizeMovementAmount(type, new BigDecimal(values[3]));
                String description = values[4].isBlank() ? "Sin descripción" : values[4];

                if (accountId <= 0 || !MOVEMENT_TYPES.contains(type)) {
                    throw new IllegalArgumentException("Movimiento incompleto");
                }

                AnnualMovement movement = new AnnualMovement(
                        accountId,
                        LegacyDateParser.parse(values[1]),
                        type,
                        amount,
                        description
                );
                if (!unique.add(movement)) {
                    discarded++;
                }
            } catch (RuntimeException ex) {
                discarded++;
            }
        }
        movements.addAll(unique);
        log.info("Movimientos web cargados: {}, descartados: {}, leídos: {}", movements.size(), discarded, read);
    }

    private void loadTransactions() {
        int read = 0;
        int discarded = 0;
        Set<LegacyTransaction> unique = new LinkedHashSet<>();
        for (String line : readLines("data/transacciones.csv")) {
            read++;
            try {
                String[] values = split(line, 4);
                long id = Long.parseLong(values[0]);
                BigDecimal amount = new BigDecimal(values[2]);
                String type = normalize(values[3]);

                if (id <= 0 || amount.signum() <= 0 || !TRANSACTION_TYPES.contains(type)) {
                    throw new IllegalArgumentException("Transacción incompleta");
                }

                LegacyTransaction transaction = new LegacyTransaction(
                        id,
                        LegacyDateParser.parse(values[1]),
                        amount,
                        type
                );
                if (!unique.add(transaction)) {
                    discarded++;
                }
            } catch (RuntimeException ex) {
                discarded++;
            }
        }
        transactions.addAll(unique);
        log.info("Transacciones web cargadas: {}, descartadas: {}, leídas: {}", transactions.size(), discarded, read);
    }

    private List<String> readLines(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().skip(1).filter(line -> !line.isBlank()).toList();
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible leer " + path, ex);
        }
    }

    private String[] split(String line, int expectedColumns) {
        String[] values = line.split(",", -1);
        if (values.length != expectedColumns) {
            throw new IllegalArgumentException("Cantidad de columnas inválida");
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toUpperCase(Locale.ROOT);
    }

    private BigDecimal normalizeMovementAmount(String type, BigDecimal amount) {
        if (amount.signum() == 0) {
            throw new IllegalArgumentException("Monto de movimiento inválido");
        }
        if ("DEPOSITO".equals(type)) {
            if (amount.signum() < 0) {
                throw new IllegalArgumentException("Depósito con monto negativo");
            }
            return amount;
        }
        return amount.abs().negate();
    }
}
