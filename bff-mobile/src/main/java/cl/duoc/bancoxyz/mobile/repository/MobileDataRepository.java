package cl.duoc.bancoxyz.mobile.repository;

import cl.duoc.bancoxyz.mobile.model.MobileAccount;
import cl.duoc.bancoxyz.mobile.model.MobileMovement;
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
public class MobileDataRepository {

    private static final Logger log = LoggerFactory.getLogger(MobileDataRepository.class);
    private static final Set<String> ACCOUNT_TYPES = Set.of("AHORRO", "PRESTAMO", "HIPOTECA");
    private static final Set<String> MOVEMENT_TYPES = Set.of("DEPOSITO", "RETIRO", "COMPRA", "PAGO");

    private final Map<Long, MobileAccount> accounts = new LinkedHashMap<>();
    private final List<MobileMovement> movements = new ArrayList<>();

    @PostConstruct
    public void loadData() {
        accounts.clear();
        movements.clear();
        loadAccounts();
        loadMovements();
    }

    public Optional<MobileAccount> findAccount(long accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public List<MobileMovement> findMovements(long accountId) {
        return movements.stream()
                .filter(movement -> movement.accountId() == accountId)
                .sorted(Comparator.comparing(MobileMovement::date).reversed())
                .toList();
    }

    public int accountCount() {
        return accounts.size();
    }

    private void loadAccounts() {
        int read = 0;
        int discarded = 0;
        for (String line : readLines("data/intereses.csv")) {
            read++;
            try {
                String[] values = split(line, 5);
                long id = Long.parseLong(values[0]);
                BigDecimal balance = new BigDecimal(values[2]);
                String type = normalize(values[4]);

                if (id <= 0 || balance.signum() < 0 || !ACCOUNT_TYPES.contains(type)) {
                    throw new IllegalArgumentException("Cuenta incompleta");
                }

                if (accounts.putIfAbsent(id, new MobileAccount(id, type, balance)) != null) {
                    discarded++;
                }
            } catch (RuntimeException ex) {
                discarded++;
            }
        }
        log.info("Cuentas móviles cargadas: {}, descartadas: {}, leídas: {}",
                accounts.size(), discarded, read);
    }

    private void loadMovements() {
        int read = 0;
        int discarded = 0;
        Set<MobileMovement> unique = new LinkedHashSet<>();
        for (String line : readLines("data/cuentas_anuales.csv")) {
            read++;
            try {
                String[] values = split(line, 5);
                long accountId = Long.parseLong(values[0]);
                String type = normalize(values[2]);
                BigDecimal amount = normalizeMovementAmount(type, new BigDecimal(values[3]));

                if (accountId <= 0 || !accounts.containsKey(accountId) || !MOVEMENT_TYPES.contains(type)) {
                    throw new IllegalArgumentException("Movimiento incompleto");
                }

                MobileMovement movement = new MobileMovement(
                        accountId,
                        LegacyDateParser.parse(values[1]),
                        type,
                        amount
                );
                if (!unique.add(movement)) {
                    discarded++;
                }
            } catch (RuntimeException ex) {
                discarded++;
            }
        }
        movements.addAll(unique);
        log.info("Movimientos móviles cargados: {}, descartados: {}, leídos: {}",
                movements.size(), discarded, read);
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
        for (int index = 0; index < values.length; index++) {
            values[index] = values[index].trim();
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
