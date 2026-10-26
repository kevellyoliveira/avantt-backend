package com.avantt_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TarefaUsuarioId implements Serializable {
    @Column(name = "tarefa_id")
    private Integer tarefaId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    public TarefaUsuarioId() {}

    public TarefaUsuarioId(Integer tarefaId, Integer usuarioId) {
        this.tarefaId = tarefaId;
        this.usuarioId = usuarioId;
    }

    public Integer getTarefaId() { return tarefaId; }
    public void setTarefaId(Integer tarefaId) { this.tarefaId = tarefaId; }
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TarefaUsuarioId that)) return false;
        return Objects.equals(tarefaId, that.tarefaId)
                && Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tarefaId, usuarioId);
    }
}
