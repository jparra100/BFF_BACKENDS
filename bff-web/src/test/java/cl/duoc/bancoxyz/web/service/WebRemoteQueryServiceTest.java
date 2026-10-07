package cl.duoc.bancoxyz.web.service;

import cl.duoc.bancoxyz.web.client.WebBankingClient;
import cl.duoc.bancoxyz.web.dto.PageResponse;
import cl.duoc.bancoxyz.web.dto.WebAccountResponse;
import cl.duoc.bancoxyz.web.repository.WebDataRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebRemoteQueryServiceTest {

    @Test
    void usesBankingServicesWhenRemoteModeIsEnabled() {
        WebBankingClient client = mock(WebBankingClient.class);
        PageResponse<WebAccountResponse> expected = PageResponse.from(List.of(
                new WebAccountResponse(1L, "Ana Torres", 0, "CORRIENTE", new BigDecimal("120000"))
        ), 0, 20);
        when(client.findAccounts("corriente", 0, 20)).thenReturn(expected);

        WebQueryService service = new WebQueryService(
                mock(WebDataRepository.class), Optional.of(client), true);

        PageResponse<WebAccountResponse> result = service.findAccounts("corriente", 0, 20);

        assertEquals(expected, result);
        verify(client).findAccounts("corriente", 0, 20);
    }
}
