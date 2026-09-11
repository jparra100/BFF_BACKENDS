package cl.duoc.bancoxyz.web.service;

import cl.duoc.bancoxyz.web.exception.ResourceNotFoundException;
import cl.duoc.bancoxyz.web.repository.WebDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebQueryServiceTest {

    private WebQueryService service;

    @BeforeEach
    void setUp() {
        WebDataRepository repository = new WebDataRepository();
        repository.loadData();
        service = new WebQueryService(repository);
    }

    @Test
    void returnsPagedAccounts() {
        var response = service.findAccounts(null, 0, 3);

        assertEquals(3, response.contenido().size());
        assertEquals(50, response.totalElementos());
        assertEquals(17, response.totalPaginas());
    }

    @Test
    void filtersTransactionsByType() {
        var response = service.findTransactions("credito", null, null, 0, 10);

        assertEquals(10, response.contenido().size());
        response.contenido().forEach(transaction -> assertEquals("CREDITO", transaction.tipo()));
    }

    @Test
    void rejectsInvalidPageSize() {
        assertThrows(IllegalArgumentException.class, () -> service.findAccounts(null, 0, 101));
    }

    @Test
    void reportsMissingAccount() {
        assertThrows(ResourceNotFoundException.class, () -> service.findAccount(9999));
    }
}

