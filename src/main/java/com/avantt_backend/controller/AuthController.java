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
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO req) {
        try {
            var res = authService.register(req);
            if (res == null) return ResponseEntity.status(409).body(MENSAGEM_EMAIL_CADASTRADO_409);
            return ResponseEntity.status(201).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO req) {
        try {
            var res = authService.login(req);
            if (res == null) return ResponseEntity.status(401).body(MENSAGEM_CREDENCIAIS_INVALIDAS_401);
            // Return token also in the Authorization response header to simplify use in Swagger UI
            String headerValue = "Bearer " + res.getToken();
            return ResponseEntity.ok().header("Authorization", headerValue).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        try {
            String authorization = request.getHeader("Authorization");

            System.out.println("======================================");
            System.out.println("AUTHORIZATION RECEBIDO: [" + authorization + "]");

            if (authorization == null || authorization.isBlank()) {
                System.out.println(">>> TOKEN AUSENTE");
                return ResponseEntity.badRequest().body("Token ausente");
            }

            String token = authorization;

            if (authorization.toLowerCase().startsWith("bearer ")) {
                token = authorization.substring(7);
            }

            token = token.trim();

            System.out.println("TOKEN EXTRAIDO: [" + token + "]");

            boolean ok = authService.logout(token);

            System.out.println("LOGOUT RETORNOU: " + ok);

            if (!ok) {
                return ResponseEntity.status(401).body("Token inválido");
            }

            System.out.println(">>> LOGOUT OK");

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Solicitar redefinição de senha")
    public ResponseEntity<?> passwordResetRequest(@RequestBody PasswordResetRequestDTO req) {
        try {
            String token = authService.requestPasswordReset(req);
            if (token == null) return ResponseEntity.status(404).body(MENSAGEM_ERRO_EMAIL_404);
            return ResponseEntity.ok(java.util.Map.of("resetToken", token));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirmar redefinição de senha")
    public ResponseEntity<?> passwordResetConfirm(@RequestBody PasswordResetConfirmDTO req) {
        try {
            boolean ok = authService.confirmPasswordReset(req);
            if (!ok) return ResponseEntity.status(400).body(MENSAGEM_ERRO_TOKEN_400);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }
}
