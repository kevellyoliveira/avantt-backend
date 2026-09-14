package com.avantt_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "marco_projeto")
public class MarcoProjeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "projeto_id", nullable = false)
    private Integer projetoId;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "concluido")
    private Boolean concluido = Boolean.FALSE;

    public MarcoProjeto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProjetoId() { return projetoId; }
    public void setProjetoId(Integer projetoId) { this.projetoId = projetoId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Boolean getConcluido() { return concluido; }
    public void setConcluido(Boolean concluido) { this.concluido = concluido; }
}
