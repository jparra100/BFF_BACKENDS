package cl.duoc.bancoxyz.atm.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String channel
) {
}
