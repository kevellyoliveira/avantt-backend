package com.avantt_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "documento")
    private String documento;

    @Column(name = "email")
    private String email;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "organizacao_id")
    private Integer organizacaoId;

    @Column(name = "data_criacao", insertable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "is_ativo")
    private Boolean isAtivo = Boolean.TRUE;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    public Cliente() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Integer getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(Integer organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Boolean getIsAtivo() {
        return isAtivo;
    }

    public void setIsAtivo(Boolean isAtivo) {
        this.isAtivo = isAtivo;
    }

    public LocalDateTime getDataDesativacao() {
        return dataDesativacao;
    }

    public void setDataDesativacao(LocalDateTime dataDesativacao) {
        this.dataDesativacao = dataDesativacao;
    }
}