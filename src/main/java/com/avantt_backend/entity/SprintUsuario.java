
package com.avantt_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sprint_usuario")
public class SprintUsuario {

    @EmbeddedId
    private SprintUsuarioId id;

    // Campo não persistido, pois a coluna papel não existe na tabela.
    @Transient
    private String papel;

    public SprintUsuario() {}

    public SprintUsuario(SprintUsuarioId id, String papel) {
        this.id = id;
        this.papel = papel;
    }

    public SprintUsuarioId getId() {
        return id;
    }

    public void setId(SprintUsuarioId id) {
        this.id = id;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }
}
