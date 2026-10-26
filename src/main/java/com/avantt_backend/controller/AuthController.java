package com.avantt_backend.controller;

import com.avantt_backend.dto.*;
import com.avantt_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> register(@RequestBody(description = "Dados para registro", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.RegisterRequestDTO.class), examples = {
                @ExampleObject(name = "Register example", value = "{\"name\": \"Ana Souza\", \"email\": \"ana.souza@email.com\", \"password\": \"Senha123!\", \"role\": \"Colaborador\"}")
            })) RegisterRequestDTO req) {
        var res = authService.register(req);
        if (res == null) throw new com.avantt_backend.exception.ConflictException(MENSAGEM_EMAIL_CADASTRADO_409);
        return ResponseEntity.status(201).body(res);
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> login(@RequestBody(description = "Credenciais de login", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.AuthRequestDTO.class), examples = {
                @ExampleObject(name = "Login example", value = "{\"email\": \"ana.souza@email.com\", \"password\": \"Senha123!\"}")
            })) AuthRequestDTO req) {
        var res = authService.login(req);
        if (res == null) throw new com.avantt_backend.exception.UnauthorizedException(MENSAGEM_CREDENCIAIS_INVALIDAS_401);
        // Return token also in the Authorization response header to simplify use in Swagger UI
        String headerValue = "Bearer " + res.getToken();
        return ResponseEntity.ok().header("Authorization", headerValue).body(res);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout realizado"),
        @ApiResponse(responseCode = "400", description = "Token ausente",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token de reset retornado"),
        @ApiResponse(responseCode = "404", description = "Email não encontrado",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> passwordResetRequest(@RequestBody(description = "Corpo: { \"email\": <email> }", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.PasswordResetRequestDTO.class), examples = {
                @ExampleObject(name = "Password reset request example", value = "{\"email\": \"ana.souza@email.com\"}")
            })) PasswordResetRequestDTO req) {
        String token = authService.requestPasswordReset(req);
        if (token == null) throw new com.avantt_backend.exception.ResourceNotFoundException(MENSAGEM_ERRO_EMAIL_404);
        return ResponseEntity.ok(java.util.Map.of("resetToken", token));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirmar redefinição de senha")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha resetada"),
        @ApiResponse(responseCode = "400", description = "Token inválido ou expirado",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> passwordResetConfirm(@RequestBody(description = "Corpo: { \"token\": <token>, \"newPassword\": <senha> }", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.PasswordResetConfirmDTO.class), examples = {
                @ExampleObject(name = "Password reset confirm example", value = "{\"token\": \"abc123\", \"newPassword\": \"NovaSenha123!\"}")
            }))  PasswordResetConfirmDTO req) {
        boolean ok = authService.confirmPasswordReset(req);
        if (!ok) throw new com.avantt_backend.exception.ApiException(MENSAGEM_ERRO_TOKEN_400);
        return ResponseEntity.ok().build();
    }
}
