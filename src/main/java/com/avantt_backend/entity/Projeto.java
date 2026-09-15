package com.avantt_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "projeto")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // maps to projeto.nome
    @Column(name = "nome", nullable = false)
    private String name;

    // maps to projeto.descricao
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String description;

    // maps to projeto.cor
    @Column(name = "cor", length = 20)
    private String color;

    // maps to projeto.status
    @Column(name = "status", length = 50)
    private String status;

    // maps to projeto.data_inicio
    @Column(name = "data_inicio")
    private LocalDate startDate;

    // maps to projeto.data_fim
    @Column(name = "data_fim")
    private LocalDate endDate;

    // maps to projeto.progresso
    @Column(name = "progresso")
    private Integer progress;

    // maps to projeto.risco (single string, nullable)
    @Column(name = "risco", length = 255)
    private String risks;

    public Projeto() {}

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getRisks() { return risks; }
    public void setRisks(String risks) { this.risks = risks; }

}
