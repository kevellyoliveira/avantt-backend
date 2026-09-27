package com.avantt_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class SprintUsuarioId implements Serializable {

    @Column(name = "sprint_id")
    private Integer sprintId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    public SprintUsuarioId() {}

    public SprintUsuarioId(Integer sprintId, Integer usuarioId) {
        this.sprintId = sprintId;
        this.usuarioId = usuarioId;
    }

    public Integer getSprintId() { return sprintId; }
    public void setSprintId(Integer sprintId) { this.sprintId = sprintId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SprintUsuarioId that = (SprintUsuarioId) o;
        return java.util.Objects.equals(sprintId, that.sprintId) && java.util.Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(sprintId, usuarioId);
    }
}
