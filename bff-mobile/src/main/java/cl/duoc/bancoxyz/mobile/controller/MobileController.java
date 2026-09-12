package cl.duoc.bancoxyz.mobile.controller;

import cl.duoc.bancoxyz.mobile.dto.MobileAccountResponse;
import cl.duoc.bancoxyz.mobile.dto.MobileMovementResponse;
import cl.duoc.bancoxyz.mobile.service.MobileQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/cuentas")
public class MobileController {

    private final MobileQueryService service;

    public MobileController(MobileQueryService service) {
        this.service = service;
    }

    @GetMapping("/{cuentaId}/resumen")
    public MobileAccountResponse getSummary(@PathVariable long cuentaId) {
        return service.getSummary(cuentaId);
    }

    @GetMapping("/{cuentaId}/movimientos-recientes")
    public List<MobileMovementResponse> getRecentMovements(
            @PathVariable long cuentaId,
            @RequestParam(defaultValue = "5") int limite
    ) {
        return service.getRecentMovements(cuentaId, limite);
    }
}
