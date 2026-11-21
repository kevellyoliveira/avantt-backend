
package com.avantt_backend.service;

import com.avantt_backend.dto.AceitarConviteRequestDTO;
import com.avantt_backend.dto.AuthResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConviteService {
    private static final Logger logger = LoggerFactory.getLogger(ConviteService.class);

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public ConviteService(
            UsuarioRepository usuarioRepository,
            EmailService emailService) {

        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    /**
     * Envia um código somente quando o usuário ainda não validou
     * seu primeiro acesso.
     */
    @Transactional
    public void convidarSeNecessario(Integer usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        if (Boolean.TRUE.equals(usuario.getPrimeiroAcessoValidado())) {
            return;
        }

        if (usuario.getEmail() == null
                || usuario.getEmail().trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O usuário não possui e-mail cadastrado."
            );
        }

        LocalDateTime agora = LocalDateTime.now();

        // Evita enviar vários códigos enquanto já existe um código válido.
        if (usuario.getConviteTokenHash() != null
                && usuario.getConviteTokenExpiry() != null
                && usuario.getConviteTokenExpiry().isAfter(agora)) {
            return;
        }

        String codigo = gerarCodigo();
        String codigoHash = gerarHash(codigo);
        logger.warn(
                "Código temporário de primeiro acesso para {}: {}",
                usuario.getEmail(),
                codigo
        );

        usuario.setConviteTokenHash(codigoHash);
        usuario.setConviteTokenExpiry(agora.plusMinutes(15));

        // Invalida qualquer sessão anterior enquanto o primeiro acesso
        // ainda não tiver sido validado.
        usuario.setAuthToken(null);
        usuario.setAuthTokenExpiry(null);

        usuarioRepository.save(usuario);

        String mensagem =
                "Olá, " + usuario.getNome() + "!\n\n"
                        + "Você foi vinculado a um projeto na plataforma AVANTT.\n\n"
                        + "Para confirmar seu primeiro acesso, informe o código abaixo "
                        + "na tela de validação após fazer login:\n\n"
                        + "Código: " + codigo + "\n\n"
                        + "Este código é válido por 15 minutos e pode ser utilizado "
                        + "uma única vez.\n\n"
                        + "Se você não esperava este acesso, entre em contato com "
                        + "o responsável pelo projeto.\n\n"
                        + "Equipe AVANTT";

        emailService.enviarEmail(
                usuario.getEmail(),
                "Código de primeiro acesso - AVANTT",
                mensagem
        );
    }

    /**
     * Valida o código e as credenciais cadastradas.
     * Após a validação, conclui o primeiro acesso e cria a sessão.
     */
    @Transactional
    public AuthResponseDTO aceitarConvite(AceitarConviteRequestDTO req) {

        if (req == null
                || req.getEmail() == null
                || req.getEmail().trim().isEmpty()
                || req.getPassword() == null
                || req.getPassword().isEmpty()
                || req.getCodigo() == null
                || !req.getCodigo().trim().matches("[A-Za-z0-9]{6}")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o e-mail, a senha cadastrada e o código de seis dígitos."
            );
        }

        String email = req.getEmail().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Dados de validação inválidos."
                ));

        if (Boolean.TRUE.equals(usuario.getPrimeiroAcessoValidado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Este primeiro acesso já foi concluído."
            );
        }

        if (usuario.getSenha() == null
                || !passwordEncoder.matches(req.getPassword(), usuario.getSenha())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "E-mail ou senha inválidos."
            );
        }

        LocalDateTime agora = LocalDateTime.now();

        if (usuario.getConviteTokenHash() == null
                || usuario.getConviteTokenExpiry() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não existe um código de primeiro acesso válido. "
                            + "Solicite ao gestor o envio de um novo código."
            );
        }

        if (!usuario.getConviteTokenExpiry().isAfter(agora)) {
            usuario.setConviteTokenHash(null);
            usuario.setConviteTokenExpiry(null);
            usuarioRepository.save(usuario);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Código expirado. Solicite ao gestor o envio de um novo código."
            );
        }

        String codigoHash = gerarHash(req.getCodigo().trim());

        if (!codigoHash.equals(usuario.getConviteTokenHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Código de primeiro acesso inválido."
            );
        }

        usuario.setPrimeiroAcessoValidado(true);

        // O código é invalidado após o uso.
        usuario.setConviteTokenHash(null);
        usuario.setConviteTokenExpiry(null);

        String authToken = UUID.randomUUID().toString();

        usuario.setAuthToken(authToken);
        usuario.setAuthTokenExpiry(agora.plusHours(24));

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new AuthResponseDTO(authToken, usuarioSalvo);
    }

    /**
     * Gera um código numérico de seis dígitos.
     */
    private String gerarCodigo() {
        int numero = secureRandom.nextInt(1_000_000);
        return String.format(Locale.ROOT, "%06d", numero);
    }

    /**
     * Gera o hash SHA-256 do código.
     */
    private String gerarHash(String codigo) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    codigo.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível gerar o hash do código.",
                    e
            );
        }
    }
}
