package cl.duoc.bancoxyz.web.controller;

import cl.duoc.bancoxyz.web.dto.PageResponse;
import cl.duoc.bancoxyz.web.dto.WebAccountDetailResponse;
import cl.duoc.bancoxyz.web.dto.WebAccountResponse;
import cl.duoc.bancoxyz.web.dto.WebMovementResponse;
import cl.duoc.bancoxyz.web.dto.WebTransactionResponse;
import cl.duoc.bancoxyz.web.service.WebQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/web")
public class WebController {

    private final WebQueryService service;

    public WebController(WebQueryService service) {
        this.service = service;
    }

    @GetMapping("/cuentas")
    public PageResponse<WebAccountResponse> findAccounts(
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio
    ) {
        return service.findAccounts(tipo, pagina, tamanio);
    }

    @GetMapping("/cuentas/{cuentaId}")
    public WebAccountDetailResponse findAccount(@PathVariable long cuentaId) {
        return service.findAccount(cuentaId);
    }

    @GetMapping("/cuentas/{cuentaId}/movimientos")
    public PageResponse<WebMovementResponse> findMovements(
            @PathVariable long cuentaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio
    ) {
        return service.findMovements(cuentaId, pagina, tamanio);
    }

    @GetMapping("/transacciones")
    public PageResponse<WebTransactionResponse> findTransactions(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio
    ) {
        return service.findTransactions(tipo, desde, hasta, pagina, tamanio);
    }
}

