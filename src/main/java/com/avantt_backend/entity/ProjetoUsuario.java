package com.avantt_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "projeto_usuario")
public class ProjetoUsuario {

    @EmbeddedId
    private ProjetoUsuarioId id;

    @Column(name = "papel")
    private String papel;

    public ProjetoUsuario() {}

    public ProjetoUsuario(ProjetoUsuarioId id, String papel) {
        this.id = id;
        this.papel = papel;
    }

    public ProjetoUsuarioId getId() { return id; }
    public void setId(ProjetoUsuarioId id) { this.id = id; }

    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }
}
