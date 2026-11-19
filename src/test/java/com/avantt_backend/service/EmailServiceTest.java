package com.avantt_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void deveEnviarEmailPeloGmail() {
        emailService.enviarEmail(
                "monicajleamari@gmail.com",
                "Teste de envio - Avantt",
                "Olá! Este é um teste de envio de e-mail do sistema Avantt. "
                        + "A configuração do Gmail está funcionando corretamente."
        );
    }
}
