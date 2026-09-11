package cl.duoc.bancoxyz.web.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String channel
) {
}

