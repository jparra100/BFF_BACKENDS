package cl.duoc.bancoxyz.atm.controller;

import cl.duoc.bancoxyz.atm.dto.BalanceResponse;
import cl.duoc.bancoxyz.atm.dto.WithdrawalRequest;
import cl.duoc.bancoxyz.atm.dto.WithdrawalResponse;
import cl.duoc.bancoxyz.atm.service.AtmService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atm/cuentas")
public class AtmController {

    private final AtmService service;

    public AtmController(AtmService service) {
        this.service = service;
    }

    @GetMapping("/{cuentaId}/saldo")
    public BalanceResponse getBalance(@PathVariable long cuentaId) {
        return service.getBalance(cuentaId);
    }

    @PostMapping("/{cuentaId}/retiros")
    public WithdrawalResponse withdraw(
            @PathVariable long cuentaId,
            @Valid @RequestBody WithdrawalRequest request
    ) {
        return service.withdraw(cuentaId, request.monto());
    }
}
