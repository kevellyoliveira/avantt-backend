package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO used by the frontend that creates an organization and a user in one call.
 */
public class RegisterOrganizacaoRequestDTO {

    // Allow either nested object (organizacao) or flattened fields (organizationName, cnpj, ...)
    @Valid
    @JsonAlias({"organizacao", "organization", "org", "empresa"})
    private OrganizacaoRequestDTO organizacao;

    @Valid
    @JsonAlias({"usuario", "user", "usuarioRequest", "usuarioDTO"})
    private RegisterRequestDTO usuario;

    // Flattened user fields (some frontends send these at root)
    private String name;
    private String email;
    private String password;
    private String role;

    // Flattened organization fields
    @JsonAlias({"organizationName", "organization_name", "nomeFantasia"})
    private String organizationName;
    private String cnpj;
    private String emailContato;
    private Boolean isAtivo;

    public RegisterOrganizacaoRequestDTO() {}

    public OrganizacaoRequestDTO getOrganizacao() { return organizacao; }
    public void setOrganizacao(OrganizacaoRequestDTO organizacao) { this.organizacao = organizacao; }

    public RegisterRequestDTO getUsuario() { return usuario; }
    public void setUsuario(RegisterRequestDTO usuario) { this.usuario = usuario; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEmailContato() { return emailContato; }
    public void setEmailContato(String emailContato) { this.emailContato = emailContato; }

    public Boolean getIsAtivo() { return isAtivo; }
    public void setIsAtivo(Boolean isAtivo) { this.isAtivo = isAtivo; }
}
