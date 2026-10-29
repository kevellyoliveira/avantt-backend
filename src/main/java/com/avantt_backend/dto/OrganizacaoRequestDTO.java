package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para criar/atualizar Organização.
 */
public class OrganizacaoRequestDTO {

    // Optional id: clients may include it on PUT requests; when present it must be positive.
    @Positive(message = "id inválido")
    private Integer id;

    @NotBlank(message = "name é obrigatório")
    @JsonAlias({"name", "nome", "nomeFantasia"})
    private String nomeFantasia;

    // Accept either 14 digits (unformatted) or the common formatted CNPJ 00.000.000/0000-00
    @Pattern(regexp = "^(\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})$", message = "cnpj inválido. Use 14 dígitos ou 00.000.000/0000-00")
    @JsonAlias({"cnpj", "document", "documento"})
    private String cnpj;

    @Email(message = "email inválido")
    @Size(max = 255)
    @JsonAlias({"email", "emailContato"})
    private String emailContato;

    @JsonAlias({"isActive", "is_ativo", "isAtivo"})
    private Boolean isAtivo;

    public OrganizacaoRequestDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEmailContato() { return emailContato; }
    public void setEmailContato(String emailContato) { this.emailContato = emailContato; }

    public Boolean getIsAtivo() { return isAtivo; }
    public void setIsAtivo(Boolean isAtivo) { this.isAtivo = isAtivo; }
}
