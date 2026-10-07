package cl.duoc.bancoxyz.accounts.dto;

import java.math.BigDecimal;

public record AccountResponse(Long id, Long customerId, String type, BigDecimal balance, String status) {
}
