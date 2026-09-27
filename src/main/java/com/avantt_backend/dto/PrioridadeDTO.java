package com.avantt_backend.dto;

public class PrioridadeDTO {
    private Integer id;
    private String nome;

    public PrioridadeDTO() {}

    public PrioridadeDTO(Integer id, String nome) { this.id = id; this.nome = nome; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
