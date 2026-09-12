package com.avantt_backend.dto;

import java.time.LocalDateTime;

public class UsuarioResponseDTO {
    private Integer id;
    private String nome;
    private String email;
    private LocalDateTime dataCadastro;
    private Boolean isAtivo;
    private Integer perfilId;
    private String cargo;
    private LocalDateTime dataDesativacao;
    private Integer organizacaoId;

    public UsuarioResponseDTO() {}

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
