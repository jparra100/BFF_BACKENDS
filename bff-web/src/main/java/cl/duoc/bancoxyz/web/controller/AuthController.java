package cl.duoc.bancoxyz.web.controller;

import cl.duoc.bancoxyz.web.dto.TokenRequest;
import cl.duoc.bancoxyz.web.dto.TokenResponse;
import cl.duoc.bancoxyz.web.security.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public TokenResponse createToken(@Valid @RequestBody TokenRequest request) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        var token = tokenService.generate(authentication);
        return new TokenResponse(token.value(), "Bearer", token.expiresIn(), token.channel());
    }
}

