package com.avantt_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO simples usado pela camada de controller/service.
 */
@Schema(description = "Representa um exemplo simples com id e nome")
public class ExampleDto {

    @Schema(description = "Identificador único", example = "1")
    private Long id;

    @Schema(description = "Nome descritivo", example = "Exemplo")
    private String name;

    public ExampleDto() {}

    public ExampleDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
