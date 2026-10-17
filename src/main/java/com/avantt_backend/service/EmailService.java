package com.avantt_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;
    private final String host;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender,
                        @Value("${avantt.mail.from:}") String from,
                        @Value("${avantt.frontend-url:http://localhost:8443}") String frontendUrl,
                        @Value("${spring.mail.host:}") String host) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl;
        this.host = host;
    }

    public void sendClientManagerInvitation(String recipient, String companyName, String token) {
        if (host == null || host.isBlank() || from == null || from.isBlank()) {
            log.warn("Convite criado para {}, mas o SMTP não está configurado; envio de e-mail foi adiado.", recipient);
            return;
        }
        String link = frontendUrl + "/convite?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("Convite para acessar a AVANTT");
        message.setText("""
                Você foi convidado para administrar a empresa %s na AVANTT.

                Acesse o link abaixo para concluir seu cadastro e definir sua senha:
                %s

                Este convite expira em 48 horas.
                """.formatted(companyName, link));
        mailSender.send(message);
    }
}
