package com.avantt_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequestDTO {

    @NotBlank(message = "senhaAtual é obrigatória")
    private String senhaAtual;

    @NotBlank(message = "novaSenha é obrigatória")
    @Size(min = 8, message = "novaSenha deve ter pelo menos 8 caracteres")
    private String novaSenha;

    public String getSenhaAtual() {
        return senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}
