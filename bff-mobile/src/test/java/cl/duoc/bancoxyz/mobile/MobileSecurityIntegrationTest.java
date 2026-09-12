package cl.duoc.bancoxyz.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        "app.security.username=mobile-test-user",
        "app.security.password=mobile-test-password",
        "app.security.jwt-secret-base64=RXN0YS1jbGF2ZS1zZS11c2Etc29sby1lbi1wcnVlYmFzLWF1dG9tYXRpemFkYXM="
})
@AutoConfigureMockMvc
class MobileSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticatesAndReturnsCompactMobileResponses() throws Exception {
        String authResponse = mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"mobile-test-user","password":"mobile-test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channel").value("MOBILE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(authResponse);
        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/api/mobile/cuentas/101/resumen")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.tipo").value("AHORRO"))
                .andExpect(jsonPath("$.saldo").value(8000))
                .andExpect(jsonPath("$.titular").doesNotExist())
                .andExpect(jsonPath("$.edadTitular").doesNotExist());

        mockMvc.perform(get("/api/mobile/cuentas/101/movimientos-recientes?limite=2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fecha").value("2024-12-22"))
                .andExpect(jsonPath("$[0].descripcion").doesNotExist());
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"mobile-test-user","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/mobile/cuentas/101/resumen"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void forbidsTokenWithWrongChannel() throws Exception {
        mockMvc.perform(get("/api/mobile/cuentas/101/resumen")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_MOBILE"),
                                new SimpleGrantedAuthority("CHANNEL_WEB"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void forbidsTokenWithWrongRole() throws Exception {
        mockMvc.perform(get("/api/mobile/cuentas/101/resumen")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_WEB"),
                                new SimpleGrantedAuthority("CHANNEL_MOBILE"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void validatesRecentMovementsLimit() throws Exception {
        mockMvc.perform(get("/api/mobile/cuentas/101/movimientos-recientes?limite=11")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_MOBILE"),
                                new SimpleGrantedAuthority("CHANNEL_MOBILE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El límite debe estar entre 1 y 10"));
    }
}
