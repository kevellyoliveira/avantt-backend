
package com.avantt_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "projeto")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cor", length = 20)
    private String color;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "organizacao_id", nullable = false)
    private Integer organizacaoId;

    @Column(name = "status")
    private String status;

    @Column(name = "data_inicio")
    private LocalDate startDate;

    @Column(name = "data_fim")
    private LocalDate endDate;

    @Column(name = "progresso")
    private Integer progress;

    public Projeto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(Integer organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }
}
