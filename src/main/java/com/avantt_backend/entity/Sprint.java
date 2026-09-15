package com.avantt_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sprint")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "projeto_id")
    private Integer projetoId;

    @Column(name = "nome")
    private String nome;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "status", length = 50)
    private String status;

    public Sprint() {}

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProjetoId() { return projetoId; }
    public void setProjetoId(Integer projetoId) { this.projetoId = projetoId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
