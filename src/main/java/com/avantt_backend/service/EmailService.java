
package com.avantt_backend.service;

import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmail(
            String destinatario,
            String assunto,
            String mensagem) {

        SimpleMailMessage email = new SimpleMailMessage();

        email.setFrom(remetente);
        email.setTo(destinatario);
        email.setSubject(assunto);
        email.setText(mensagem);

        mailSender.send(email);
    }

    /**
     * Envia o e-mail de recuperação de senha.
     */
    public void enviarEmailRecuperacaoSenha(
            String destinatario,
            String nomeUsuario,
            String linkRecuperacao) {

        String assunto = "Recuperação de senha - Avantt";

        String mensagem = "Olá, " + nomeUsuario + "!\n\n"
                + "Recebemos uma solicitação para redefinir a senha "
                + "da sua conta no Avantt.\n\n"
                + "Para cadastrar uma nova senha, acesse o link abaixo:\n\n"
                + linkRecuperacao + "\n\n"
                + "Este link é válido por 2 horas e poderá ser utilizado "
                + "apenas uma vez.\n\n"
                + "Se você não solicitou a redefinição da senha, "
                + "ignore este e-mail.\n\n"
                + "Atenciosamente,\n"
                + "Equipe Avantt.";

        enviarEmail(destinatario, assunto, mensagem);
    }

    /**
     * Envia aviso de prazo de conclusão de projeto.
     */
    public void enviarAvisoPrazoProjeto(
            Projeto projeto,
            Usuario gestor) {

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String dataFim = projeto.getEndDate().format(formato);

        String assunto =
                "O prazo do seu projeto está chegando ao fim";

        String mensagem = "Olá, " + gestor.getNome() + "!\n\n"
                + "O prazo previsto para conclusão do projeto "
                + "\"" + projeto.getName() + "\" está chegando ao fim.\n\n"
                + "Data prevista para término: " + dataFim + "\n\n"
                + "Deseja reestimar o prazo deste projeto?\n\n"
                + "Acesse o Avantt para consultar o projeto e, "
                + "se necessário, informar uma nova previsão de término.\n\n"
                + "Atenciosamente,\n"
                + "Equipe Avantt.";

        enviarEmail(gestor.getEmail(), assunto, mensagem);
    }
}
