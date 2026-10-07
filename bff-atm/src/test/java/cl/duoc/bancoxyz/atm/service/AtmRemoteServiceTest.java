package cl.duoc.bancoxyz.atm.service;

import cl.duoc.bancoxyz.atm.client.AtmBankingClient;
import cl.duoc.bancoxyz.atm.dto.BalanceResponse;
import cl.duoc.bancoxyz.atm.dto.WithdrawalResponse;
import cl.duoc.bancoxyz.atm.repository.AtmAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtmRemoteServiceTest {

    @Test
    void delegatesBalanceToCentralAccountService() {
        AtmBankingClient client = mock(AtmBankingClient.class);
        when(client.getBalance(1L)).thenReturn(new BalanceResponse(1L, new BigDecimal("45000")));
        AtmService service = new AtmService(mock(AtmAccountRepository.class), Optional.of(client), true);

        assertThat(service.getBalance(1L).saldoDisponible()).isEqualByComparingTo("45000");
        verify(client).getBalance(1L);
    }

    @Test
    void delegatesWithdrawalToPaymentService() {
        AtmBankingClient client = mock(AtmBankingClient.class);
        BigDecimal amount = new BigDecimal("10000");
        when(client.withdraw(1L, amount))
                .thenReturn(new WithdrawalResponse(1L, amount, new BigDecimal("35000")));
        AtmService service = new AtmService(mock(AtmAccountRepository.class), Optional.of(client), true);

        assertThat(service.withdraw(1L, amount).saldoDisponible()).isEqualByComparingTo("35000");
        verify(client).withdraw(1L, amount);
    }
}
