package cl.duoc.bancoxyz.web;

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
        "app.security.username=web-test-user",
        "app.security.password=web-test-password",
        "app.security.jwt-secret-base64=RXN0YS1jbGF2ZS1zZS11c2Etc29sby1lbi1wcnVlYmFzLWF1dG9tYXRpemFkYXM="
})
@AutoConfigureMockMvc
class WebSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticatesAndUsesWebToken() throws Exception {
        String authResponse = mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"web-test-user","password":"web-test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.channel").value("WEB"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(authResponse);
        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/api/web/cuentas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(50));
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"web-test-user","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/web/cuentas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void forbidsTokenFromAnotherChannel() throws Exception {
        mockMvc.perform(get("/api/web/cuentas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ATM"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
