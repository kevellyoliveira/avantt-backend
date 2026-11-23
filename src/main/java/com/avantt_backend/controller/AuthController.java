
package com.avantt_backend.controller;

import com.avantt_backend.dto.*;
import com.avantt_backend.service.AuthService;
import com.avantt_backend.service.ConviteService;
import com.avantt_backend.service.OrganizacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

import static com.avantt_backend.util.Mensagens.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticação e gerenciamento de sessão")
public class AuthController {

    private final AuthService authService;
    private final OrganizacaoService organizacaoService;
    private final ConviteService conviteService;

    public AuthController(
            AuthService authService,
            OrganizacaoService organizacaoService,
            ConviteService conviteService) {

        this.authService = authService;
        this.organizacaoService = organizacaoService;
        this.conviteService = conviteService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> register(
            @RequestBody(
                    description = "Dados para registro",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequestDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Register example",
                                            value = "{\"name\": \"Ana Souza\", \"email\": \"ana.souza@email.com\", \"password\": \"Senha123!\", \"role\": \"Colaborador\"}"
                                    )
                            }
                    )
            )
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            RegisterRequestDTO req) {

        var res = authService.register(req);

        if (res == null) {
            throw new com.avantt_backend.exception.ConflictException(
                    MENSAGEM_EMAIL_CADASTRADO_409);
        }

        java.util.Map<String, Object> resp =
                new java.util.LinkedHashMap<>();

        resp.put("usuario", res);
        resp.put("user", res);

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado ou primeiro acesso pendente"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> login(
            @RequestBody(
                    description = "Credenciais de login",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthRequestDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Login example",
                                            value = "{\"email\": \"ana.souza@email.com\", \"password\": \"Senha123!\"}"
                                    )
                            }
                    )
            )
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            AuthRequestDTO req) {

        var res = authService.login(req);

        if (res == null) {
            throw new com.avantt_backend.exception.UnauthorizedException(
                    MENSAGEM_CREDENCIAIS_INVALIDAS_401);
        }

        if (res.isRequiresFirstAccessValidation()) {
            return ResponseEntity.ok(res);
        }

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + res.getToken())
                .body(res);
    }

    @PostMapping("/convite/aceitar")
    @Operation(summary = "Validar código e concluir primeiro acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primeiro acesso validado"),
            @ApiResponse(responseCode = "400", description = "Código inválido ou expirado"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> aceitarConvite(
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            AceitarConviteRequestDTO req) {

        var res = conviteService.aceitarConvite(req);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + res.getToken())
                .body(res);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado"),
            @ApiResponse(responseCode = "400", description = "Token ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || authorization.isBlank()) {
            throw new IllegalArgumentException(MENSAGEM_TOKEN_AUSENTE);
        }

        String token = authorization;

        if (authorization.toLowerCase().startsWith("bearer ")) {
            token = authorization.substring(7);
        }

        token = token.trim();

        boolean ok = authService.logout(token);

        if (!ok) {
            throw new com.avantt_backend.exception.UnauthorizedException(
                    MENSAGEM_TOKEN_INVALIDO);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Solicitar redefinição de senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação processada. Se o e-mail estiver cadastrado, será enviado um link de recuperação.")
    })
    public ResponseEntity<?> passwordResetRequest(
            @RequestBody(
                    description = "Corpo: { \"email\": \"ana.souza@email.com\" }",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PasswordResetRequestDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Password reset request example",
                                            value = "{\"email\": \"ana.souza@email.com\"}"
                                    )
                            }
                    )
            )
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            PasswordResetRequestDTO req) {

        authService.requestPasswordReset(req);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Se o e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha."
                )
        );
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirmar redefinição de senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha resetada"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> passwordResetConfirm(
            @RequestBody(
                    description = "Corpo: { \"token\": \"abc123\", \"newPassword\": \"NovaSenha123!\" }",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PasswordResetConfirmDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Password reset confirm example",
                                            value = "{\"token\": \"abc123\", \"newPassword\": \"NovaSenha123!\"}"
                                    )
                            }
                    )
            )
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            PasswordResetConfirmDTO req) {

        boolean ok = authService.confirmPasswordReset(req);

        if (!ok) {
            throw new com.avantt_backend.exception.ApiException(
                    MENSAGEM_ERRO_TOKEN_400);
        }

        // Retorna true porque o frontend espera um booleano no corpo.
        return ResponseEntity.ok(true);
    }

    @GetMapping("/register-organizacao")
    public ResponseEntity<ErrorResponse> registerOrganizacaoGet() {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                "Use POST /api/auth/register-organizacao to create organization and user"
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Allow", "POST, OPTIONS")
                .body(body);
    }

    @PostMapping("/register-organizacao")
    @Transactional
    @Operation(summary = "Registrar organização e usuário (compatibilidade com frontend)")
    public ResponseEntity<?> registerOrganizacao(
            @RequestBody(
                    description = "Dados de organização e usuário",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterOrganizacaoRequestDTO.class)
                    )
            )
            @jakarta.validation.Valid
            @org.springframework.web.bind.annotation.RequestBody
            RegisterOrganizacaoRequestDTO req) {

        var orgReq = req.getOrganizacao();

        if (orgReq == null) {
            orgReq = new OrganizacaoRequestDTO();

            orgReq.setNomeFantasia(req.getOrganizationName());
            orgReq.setCnpj(req.getCnpj());
            orgReq.setEmailContato(req.getEmailContato());
            orgReq.setIsAtivo(req.getIsAtivo());
        }

        var userReq = req.getUsuario();

        if (userReq == null) {
            userReq = new RegisterRequestDTO();

            userReq.setName(req.getName());
            userReq.setEmail(req.getEmail());
            userReq.setPassword(req.getPassword());
            userReq.setRole(req.getRole());
        }

        /*
         * Procura a organização pelo CNPJ.
         * Se já existir, utiliza o ID existente.
         * Se não existir, cria uma nova organização.
         */
        var organization = organizacaoService.findOrCreateByCnpj(orgReq);

        Integer orgId;

        try {
            orgId = organization.getId() == null
                    ? null
                    : Integer.valueOf(organization.getId());

        } catch (NumberFormatException ex) {
            throw new com.avantt_backend.exception.ApiException(
                    "Erro ao identificar a organização."
            );
        }

        /*
         * Registra o administrador na organização encontrada ou criada.
         */
        var createdUser = authService.register(userReq, orgId);

        if (createdUser == null) {
            throw new com.avantt_backend.exception.ConflictException(
                    MENSAGEM_EMAIL_CADASTRADO_409);
        }

        java.util.Map<String, Object> resp =
                new java.util.LinkedHashMap<>();

        resp.put("organizacao", organization);
        resp.put("organization", organization);
        resp.put("usuario", createdUser);
        resp.put("user", createdUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
