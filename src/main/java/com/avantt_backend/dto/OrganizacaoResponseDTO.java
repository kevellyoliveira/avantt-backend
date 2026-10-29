package com.avantt_backend.dto;

import java.time.LocalDateTime;

/**
 * DTO de resposta para Organização (formato usado pelo frontend).
 */
public class OrganizacaoResponseDTO {
    private String id;
    private String name;
    private String cnpj;
    private String email;
    private Boolean isActive;
    private LocalDateTime dataDesativacao;
    // Additional fields expected by frontend
    private String tradeName;
    private String legalName;
    private String contact;
    private String phone;
    private LocalDateTime createdAt;

    public OrganizacaoResponseDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Backwards-compatible accessors for frontends expecting Portuguese field names
    public String getNomeFantasia() { return name; }
    public void setNomeFantasia(String nomeFantasia) { this.name = nomeFantasia; }
    
    // Frontend expects tradeName / legalName / contact / phone / createdAt
    public String getTradeName() { return tradeName == null ? name : tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailContato() { return email; }
    public void setEmailContato(String emailContato) { this.email = emailContato; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsAtivo() { return isActive; }
    public void setIsAtivo(Boolean isAtivo) { this.isActive = isAtivo; }

    public LocalDateTime getDataDesativacao() { return dataDesativacao; }
    public void setDataDesativacao(LocalDateTime dataDesativacao) { this.dataDesativacao = dataDesativacao; }
}
