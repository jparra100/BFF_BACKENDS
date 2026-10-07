package cl.duoc.bancoxyz.mobile.service;

import cl.duoc.bancoxyz.mobile.client.MobileBankingClient;
import cl.duoc.bancoxyz.mobile.dto.MobileAccountResponse;
import cl.duoc.bancoxyz.mobile.dto.MobileMovementResponse;
import cl.duoc.bancoxyz.mobile.repository.MobileDataRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobileRemoteQueryServiceTest {

    @Test
    void delegatesCompactSummaryToAccountService() {
        MobileBankingClient client = mock(MobileBankingClient.class);
        when(client.getSummary(1L))
                .thenReturn(new MobileAccountResponse(1L, "AHORRO", new BigDecimal("25000")));
        MobileQueryService service = new MobileQueryService(
                mock(MobileDataRepository.class), Optional.of(client), true);

        assertThat(service.getSummary(1L).saldo()).isEqualByComparingTo("25000");
        verify(client).getSummary(1L);
    }

    @Test
    void delegatesLimitedMovementsToPaymentService() {
        MobileBankingClient client = mock(MobileBankingClient.class);
        var expected = List.of(new MobileMovementResponse(
                LocalDate.of(2026, 10, 7), "TRANSFER", new BigDecimal("-5000")));
        when(client.getRecentMovements(1L, 5)).thenReturn(expected);
        MobileQueryService service = new MobileQueryService(
                mock(MobileDataRepository.class), Optional.of(client), true);

        assertThat(service.getRecentMovements(1L, 5)).isEqualTo(expected);
        verify(client).getRecentMovements(1L, 5);
    }
}
