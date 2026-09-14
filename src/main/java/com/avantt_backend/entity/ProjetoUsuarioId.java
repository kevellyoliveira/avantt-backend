package com.avantt_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ProjetoUsuarioId implements Serializable {

    @Column(name = "projeto_id")
    private Integer projetoId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    public ProjetoUsuarioId() {}

    public ProjetoUsuarioId(Integer projetoId, Integer usuarioId) {
        this.projetoId = projetoId;
        this.usuarioId = usuarioId;
    }

    public Integer getProjetoId() { return projetoId; }
    public void setProjetoId(Integer projetoId) { this.projetoId = projetoId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjetoUsuarioId that = (ProjetoUsuarioId) o;
        return java.util.Objects.equals(projetoId, that.projetoId) && java.util.Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(projetoId, usuarioId);
    }
}
