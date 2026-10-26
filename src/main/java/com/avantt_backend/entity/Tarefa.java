package com.avantt_backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tarefa")
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Transient
    private String nome;

    // foreign keys
    @Column(name = "projeto_id")
    private Integer projetoId;

    @Column(name = "sprint_id")
    private Integer sprintId;

    @Transient
    private String project;

    @Transient
    private String sprint;

    @Transient
    private Integer assignee;

    @Transient
    private String avatar;

    @Transient
    private String avatarColor;

    @Transient
    private String priority;

    @Transient
    private String status;

    @Column(name = "prioridade_id")
    private Integer prioridadeId;

    @Column(name = "status_id")
    private Integer statusId;

    @Transient
    private Integer daysDelayed;

    @Column(name = "data_fim")
    private LocalDate plannedEnd;

    @Transient
    private Integer estimatedHours;

    @Column(name = "horas_estimadas", precision = 10, scale = 2)
    private BigDecimal estimatedHoursValue;

    @Transient
    private String blockedBy;

    @Transient
    private String tags; // JSON array

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    public Tarefa() {}

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }

    public Integer getProjetoId() { return projetoId; }
    public void setProjetoId(Integer projetoId) { this.projetoId = projetoId; }

    public String getSprint() { return sprint; }
    public void setSprint(String sprint) { this.sprint = sprint; }

    public Integer getSprintId() { return sprintId; }
    public void setSprintId(Integer sprintId) { this.sprintId = sprintId; }

    public Integer getAssignee() { return assignee; }
    public void setAssignee(Integer assignee) { this.assignee = assignee; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getPrioridadeId() { return prioridadeId; }
    public void setPrioridadeId(Integer prioridadeId) { this.prioridadeId = prioridadeId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public Integer getDaysDelayed() { return daysDelayed; }
    public void setDaysDelayed(Integer daysDelayed) { this.daysDelayed = daysDelayed; }

    public LocalDate getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getBlockedBy() { return blockedBy; }
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @PrePersist
    @PreUpdate
    private void synchronizeTitle() {
        if (titulo == null) {
            titulo = nome;
        }
        nome = titulo;
        if (estimatedHours != null) {
            estimatedHoursValue = BigDecimal.valueOf(estimatedHours);
        }
    }

    @PostLoad
    private void synchronizeTitleAfterLoad() {
        nome = titulo;
        estimatedHours = estimatedHoursValue == null ? null : estimatedHoursValue.intValue();
    }
}
