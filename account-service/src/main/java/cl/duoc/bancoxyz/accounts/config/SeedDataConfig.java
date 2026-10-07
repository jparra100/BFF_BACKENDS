package cl.duoc.bancoxyz.accounts.config;

import cl.duoc.bancoxyz.accounts.model.BankAccount;
import cl.duoc.bancoxyz.accounts.repository.BankAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class SeedDataConfig {
    @Bean CommandLineRunner seedAccounts(BankAccountRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new BankAccount(1L, "AHORRO", new BigDecimal("850000")));
                repository.save(new BankAccount(2L, "CORRIENTE", new BigDecimal("420000")));
                repository.save(new BankAccount(3L, "AHORRO", new BigDecimal("1250000")));
            }
        };
    }
}
