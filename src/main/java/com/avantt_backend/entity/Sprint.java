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

    @Transient
    private Integer statusId;

    @Column(name = "objetivo", columnDefinition = "TEXT")
    private String objetivo;

    @Column(name = "status")
    private String status;

    @Transient
    // Progresso da sprint (0..100). Calculado pela regra:
    // - Contagem de tarefas concluídas / contagem de tarefas (excluindo as canceladas), arredondado para inteiro.
    // - Atualizado por recalculateAndPersistProgress(...) em SprintService e exposto em SprintResponseDTO.progress.
    private Integer progresso;

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

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgresso() { return progresso; }
    public void setProgresso(Integer progresso) { this.progresso = progresso; }

    @PrePersist
    @PreUpdate
    private void synchronizeStatus() {
        if (statusId != null) {
            status = switch (statusId) {
                case 2 -> "Em andamento";
                case 3 -> "Finalizada";
                default -> "Planejada";
            };
        }
    }

    @PostLoad
    private void synchronizeStatusId() {
        statusId = switch (status) {
            case "Em andamento" -> 2;
            case "Finalizada" -> 3;
            default -> 1;
        };
    }
}
