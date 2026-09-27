package com.avantt_backend.controller;

import com.avantt_backend.dto.*;
import com.avantt_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import static com.avantt_backend.util.Mensagens.*;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticação e gerenciamento de sessão")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email já cadastrado",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO req) {
        var res = authService.register(req);
        if (res == null) throw new com.avantt_backend.exception.ConflictException(MENSAGEM_EMAIL_CADASTRADO_409);
        return ResponseEntity.status(201).body(res);
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO req) {
        var res = authService.login(req);
        if (res == null) throw new com.avantt_backend.exception.UnauthorizedException(MENSAGEM_CREDENCIAIS_INVALIDAS_401);
        // Return token also in the Authorization response header to simplify use in Swagger UI
        String headerValue = "Bearer " + res.getToken();
        return ResponseEntity.ok().header("Authorization", headerValue).body(res);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout realizado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token ausente",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token inválido",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || authorization.isBlank()) {
            throw new IllegalArgumentException(MENSAGEM_TOKEN_AUSENTE);
        }

        String token = authorization;
        if (authorization.toLowerCase().startsWith("bearer ")) token = authorization.substring(7);
        token = token.trim();

        boolean ok = authService.logout(token);
        if (!ok) throw new com.avantt_backend.exception.UnauthorizedException(MENSAGEM_TOKEN_INVALIDO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Solicitar redefinição de senha")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token de reset retornado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Email não encontrado",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> passwordResetRequest(@RequestBody PasswordResetRequestDTO req) {
        String token = authService.requestPasswordReset(req);
        if (token == null) throw new com.avantt_backend.exception.ResourceNotFoundException(MENSAGEM_ERRO_EMAIL_404);
        return ResponseEntity.ok(java.util.Map.of("resetToken", token));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirmar redefinição de senha")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Senha resetada"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token inválido ou expirado",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> passwordResetConfirm(@RequestBody PasswordResetConfirmDTO req) {
        boolean ok = authService.confirmPasswordReset(req);
        if (!ok) throw new com.avantt_backend.exception.ApiException(MENSAGEM_ERRO_TOKEN_400);
        return ResponseEntity.ok().build();
    }
}
