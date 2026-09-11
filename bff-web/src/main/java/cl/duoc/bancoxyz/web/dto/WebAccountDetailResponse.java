package cl.duoc.bancoxyz.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record WebAccountDetailResponse(
        long cuentaId,
        String titular,
        int edadTitular,
        String tipoCuenta,
        BigDecimal saldo,
        List<WebMovementResponse> movimientos
) {
}

