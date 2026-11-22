
package com.avantt_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {

        emailService = new EmailService(mailSender);

        ReflectionTestUtils.setField(
                emailService,
                "remetente",
                "avantt@gmail.com"
        );
    }

    @Test
    void deveEnviarEmail() {

        emailService.enviarEmail(
                "monicajleamari@gmail.com",
                "Teste de envio - Avantt",
                "Teste de envio de e-mail."
        );

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage email = captor.getValue();

        assertEquals(
                "monicajleamari@gmail.com",
                email.getTo()[0]
        );

        assertEquals(
                "Teste de envio - Avantt",
                email.getSubject()
        );

        assertEquals(
                "Teste de envio de e-mail.",
                email.getText()
        );
    }

    @Test
    void deveEnviarEmailDeRecuperacaoDeSenha() {

        String destinatario = "monicajleamari@gmail.com";
        String nomeUsuario = "Monica";
        String link = "http://localhost:5173/?resetToken=token-teste";

        emailService.enviarEmailRecuperacaoSenha(
                destinatario,
                nomeUsuario,
                link
        );

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage email = captor.getValue();

        assertEquals(destinatario, email.getTo()[0]);

        assertEquals(
                "Recuperação de senha - Avantt",
                email.getSubject()
        );

        assertTrue(email.getText().contains(nomeUsuario));
        assertTrue(email.getText().contains(link));
        assertTrue(email.getText().contains("2 horas"));
    }
}
