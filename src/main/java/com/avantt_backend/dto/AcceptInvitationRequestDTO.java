package com.avantt_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AcceptInvitationRequestDTO {
    @NotBlank(message = "token é obrigatório")
    private String token;
    @NotBlank(message = "nome é obrigatório")
    private String name;
    @NotBlank(message = "senha é obrigatória")
    @Size(min = 8, message = "senha deve ter pelo menos 8 caracteres")
    private String password;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
