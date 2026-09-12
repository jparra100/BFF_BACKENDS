package cl.duoc.bancoxyz.atm.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contraseña es obligatoria") String password
) {
}
