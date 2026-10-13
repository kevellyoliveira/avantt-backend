package com.avantt_backend.dto;

import jakarta.validation.constraints.NotNull;

public class SprintUsuarioRequestDTO {
    @NotNull(message = "usuarioId é obrigatório")
    private Integer usuarioId;

    public SprintUsuarioRequestDTO() {}

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
}
