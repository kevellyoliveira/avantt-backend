package com.avantt_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // Let the DB manage this column by default. We mark it as non-insertable so JPA does not try to set it.
    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "is_ativo")
    private Boolean isAtivo = Boolean.TRUE;

    @Column(name = "perfil_id")
    private Integer perfilId;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "organizacao_id")
    private Integer organizacaoId;

    public Usuario() {
    }

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public Boolean getIsAtivo() { return isAtivo; }
    public void setIsAtivo(Boolean isAtivo) { this.isAtivo = isAtivo; }

    public Integer getPerfilId() { return perfilId; }
    public void setPerfilId(Integer perfilId) { this.perfilId = perfilId; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public LocalDateTime getDataDesativacao() { return dataDesativacao; }
    public void setDataDesativacao(LocalDateTime dataDesativacao) { this.dataDesativacao = dataDesativacao; }

    public Integer getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(Integer organizacaoId) { this.organizacaoId = organizacaoId; }
}
