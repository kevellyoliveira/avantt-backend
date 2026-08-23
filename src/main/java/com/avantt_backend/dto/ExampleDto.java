package com.avantt_backend.dto;

/**
 * DTO simples usado pela camada de controller/service.
 */
public class ExampleDto {

    private Long id;
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
