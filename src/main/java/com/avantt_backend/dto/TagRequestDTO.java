package com.avantt_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagRequestDTO {
    @NotBlank(message = "nome é obrigatório")
    @Size(max = 50, message = "nome deve ter no máximo 50 caracteres")
    private String nome;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
