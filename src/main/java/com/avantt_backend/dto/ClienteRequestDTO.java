package com.avantt_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class ClienteRequestDTO {
    @NotBlank(message = "nome é obrigatório")
    private String nome;
    private String documento;
    @NotBlank(message = "email do gestor é obrigatório")
    @Email(message = "email inválido")
    private String email;
    private String telefone;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
