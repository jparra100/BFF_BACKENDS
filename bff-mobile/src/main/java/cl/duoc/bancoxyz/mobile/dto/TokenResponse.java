package cl.duoc.bancoxyz.mobile.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String channel
) {
}
