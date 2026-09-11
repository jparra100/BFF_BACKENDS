package cl.duoc.bancoxyz.atm.repository;

import cl.duoc.bancoxyz.atm.model.AtmAccount;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AtmAccountRepository {

    private static final Logger log = LoggerFactory.getLogger(AtmAccountRepository.class);
    private final Map<Long, AtmAccount> accounts = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadAccounts() {
        Map<Long, AtmAccount> loadedAccounts = new LinkedHashMap<>();
        int read = 0;
        int discarded = 0;

        ClassPathResource resource = new ClassPathResource("data/intereses.csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                read++;
                try {
                    AtmAccount account = parseAccount(line);
                    if (loadedAccounts.putIfAbsent(account.accountId(), account) != null) {
                        discarded++;
                    }
                } catch (RuntimeException ex) {
                    discarded++;
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible leer data/intereses.csv", ex);
        }

        accounts.clear();
        accounts.putAll(loadedAccounts);
        log.info("Cuentas de cajero cargadas: {}, descartadas: {}, leídas: {}",
                accounts.size(), discarded, read);
    }

    public Optional<AtmAccount> findById(long accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public int count() {
        return accounts.size();
    }

    private AtmAccount parseAccount(String line) {
        String[] values = line.split(",", -1);
        if (values.length != 5) {
            throw new IllegalArgumentException("Cantidad de columnas inválida");
        }
        for (int index = 0; index < values.length; index++) {
            values[index] = values[index].trim();
        }

        long accountId = Long.parseLong(values[0]);
        BigDecimal balance = new BigDecimal(values[2]);

        if (accountId <= 0 || balance.signum() < 0) {
            throw new IllegalArgumentException("Cuenta incompleta");
        }

        return new AtmAccount(accountId, balance);
    }
}
