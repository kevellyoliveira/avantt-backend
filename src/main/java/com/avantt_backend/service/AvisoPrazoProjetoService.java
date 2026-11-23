
package com.avantt_backend.service;

import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class AvisoPrazoProjetoService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AvisoPrazoProjetoService.class);

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public AvisoPrazoProjetoService(
            ProjetoRepository projetoRepository,
            UsuarioRepository usuarioRepository,
            EmailService emailService) {

        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    // Executa todos os dias às 9h, horário de São Paulo.
    @Scheduled(
            cron = "0 0 9 * * *",
            zone = "America/Sao_Paulo"
    )
    @Transactional
    public void verificarPrazosProjetos() {

        LocalDate amanha = LocalDate.now(
                java.time.ZoneId.of("America/Sao_Paulo")
        ).plusDays(1);

        List<Projeto> projetos =
                projetoRepository.findByEndDate(amanha);

        for (Projeto projeto : projetos) {

            try {
                processarProjeto(projeto, amanha);

            } catch (Exception e) {
                LOGGER.error(
                        "Erro ao enviar aviso de prazo do projeto ID {}",
                        projeto.getId(),
                        e
                );
            }
        }
    }

    private void processarProjeto(
            Projeto projeto,
            LocalDate dataFim) {

        if (projeto.getCriadoPorId() == null) {
            LOGGER.warn(
                    "Projeto {} não possui usuário criador cadastrado.",
                    projeto.getId()
            );
            return;
        }

        if (projeto.getEndDate() == null) {
            return;
        }

        if (projeto.getAvisoPrazoEnviadoPara() != null
                && projeto.getAvisoPrazoEnviadoPara()
                .equals(projeto.getEndDate())) {

            return;
        }

        if (isStatusFinalizado(projeto.getStatus())) {
            return;
        }

        Usuario gestor = usuarioRepository
                .findById(projeto.getCriadoPorId())
                .orElse(null);

        if (gestor == null || gestor.getEmail() == null
                || gestor.getEmail().isBlank()) {

            LOGGER.warn(
                    "Não foi possível localizar o criador do projeto {}.",
                    projeto.getId()
            );
            return;
        }

        emailService.enviarAvisoPrazoProjeto(projeto, gestor);

        // Só registra o aviso depois que o envio do e-mail terminar.
        projeto.setAvisoPrazoEnviadoPara(dataFim);
        projetoRepository.save(projeto);

        LOGGER.info(
                "Aviso de prazo enviado para o projeto {} e usuário {}.",
                projeto.getId(),
                gestor.getId()
        );
    }

    private boolean isStatusFinalizado(String status) {

        if (status == null) {
            return false;
        }

        String normalizado = Normalizer
                .normalize(status.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        return normalizado.equals("concluida")
                || normalizado.equals("concluido")
                || normalizado.equals("cancelada")
                || normalizado.equals("cancelado");
    }
}
