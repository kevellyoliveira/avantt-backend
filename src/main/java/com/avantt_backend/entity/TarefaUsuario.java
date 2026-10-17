package com.avantt_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tarefa_usuario")
public class TarefaUsuario {
    @EmbeddedId
    private TarefaUsuarioId id;

    @Column(name = "papel")
    private String papel = "Responsável";

    public TarefaUsuario() {}

    public TarefaUsuario(TarefaUsuarioId id, String papel) {
        this.id = id;
        this.papel = papel;
    }

    public TarefaUsuarioId getId() { return id; }
    public void setId(TarefaUsuarioId id) { this.id = id; }
    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }
}
