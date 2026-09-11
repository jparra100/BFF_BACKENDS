package cl.duoc.bancoxyz.web.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebDataRepositoryTest {

    private WebDataRepository repository;

    @BeforeEach
    void setUp() {
        repository = new WebDataRepository();
        repository.loadData();
    }

    @Test
    void loadsValidLegacyData() {
        long movementCount = repository.findAllAccounts().stream()
                .mapToLong(account -> repository.findMovements(account.accountId()).size())
                .sum();

        assertEquals(50, repository.findAllAccounts().size());
        assertEquals(859, movementCount);
        assertEquals(401, repository.findAllTransactions().size());
    }

    @Test
    void keepsFirstValidRecordForEachAccount() {
        var account = repository.findAccount(101).orElseThrow();

        assertEquals("Diana Prince", account.holderName());
        assertEquals(new BigDecimal("8000"), account.balance());
        assertEquals(35, account.holderAge());
        assertEquals("AHORRO", account.accountType());
    }

    @Test
    void normalizesMovementSignsAndDescriptions() {
        var movements = repository.findMovements(101);

        assertTrue(movements.stream()
                .filter(movement -> movement.type().equals("RETIRO"))
                .allMatch(movement -> movement.amount().signum() < 0));
        assertTrue(movements.stream()
                .filter(movement -> movement.type().equals("DEPOSITO"))
                .allMatch(movement -> movement.amount().signum() > 0));
        assertTrue(movements.stream().anyMatch(movement -> movement.description().equals("Sin descripción")));
    }
}

