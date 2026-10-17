package com.avantt_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {
    @NotBlank(message = "name é obrigatório")
    private String name;

    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    private String email;

    @NotBlank(message = "password é obrigatório")
    @Size(min = 8, message = "password deve ter pelo menos 8 caracteres")
    private String password;

    @NotBlank(message = "role é obrigatório")
    private String role;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
