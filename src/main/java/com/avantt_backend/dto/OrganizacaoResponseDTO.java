package com.avantt_backend.dto;

import java.time.LocalDateTime;

/**
 * DTO de resposta para Organização (formato usado pelo frontend).
 */
public class OrganizacaoResponseDTO {
    private String id;
    private String nomeFantasia;
    private String cnpj;
    private String emailContato;
    private Boolean isAtivo;
    private LocalDateTime dataDesativacao;

    public OrganizacaoResponseDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEmailContato() { return emailContato; }
    public void setEmailContato(String emailContato) { this.emailContato = emailContato; }

    public Boolean getIsAtivo() { return isAtivo; }
    public void setIsAtivo(Boolean isAtivo) { this.isAtivo = isAtivo; }

    public LocalDateTime getDataDesativacao() { return dataDesativacao; }
    public void setDataDesativacao(LocalDateTime dataDesativacao) { this.dataDesativacao = dataDesativacao; }
}
