package cl.duoc.bancoxyz.atm;

import cl.duoc.bancoxyz.atm.repository.AtmAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "server.ssl.enabled=false",
        "server.ssl.key-store-password=solo-para-pruebas",
        "app.security.username=atm-test-user",
        "app.security.password=atm-test-password",
        "app.security.jwt-secret-base64=RXN0YS1jbGF2ZS1zZS11c2Etc29sby1lbi1wcnVlYmFzLWF1dG9tYXRpemFkYXM="
})
@AutoConfigureMockMvc
class AtmSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AtmAccountRepository repository;

    @BeforeEach
    void resetBalances() {
        repository.loadAccounts();
    }

    @Test
    void authenticatesAndUsesAtmToken() throws Exception {
        String authResponse = mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"atm-test-user","password":"atm-test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.channel").value("ATM"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(authResponse);
        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/api/atm/cuentas/101/saldo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoDisponible").value(8000));

        mockMvc.perform(post("/api/atm/cuentas/101/retiros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto":1000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoDisponible").value(7000));
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"atm-test-user","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/atm/cuentas/101/saldo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void forbidsTokenWithWrongChannel() throws Exception {
        mockMvc.perform(get("/api/atm/cuentas/101/saldo")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ATM"),
                                new SimpleGrantedAuthority("CHANNEL_WEB"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void forbidsTokenWithWrongRole() throws Exception {
        mockMvc.perform(get("/api/atm/cuentas/101/saldo")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_WEB"),
                                new SimpleGrantedAuthority("CHANNEL_ATM"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void validatesWithdrawalAmount() throws Exception {
        mockMvc.perform(post("/api/atm/cuentas/101/retiros")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ATM"),
                                new SimpleGrantedAuthority("CHANNEL_ATM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El monto debe ser mayor que cero"));
    }
}
