package com.avantt_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tarefa")
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;
    // coluna nome (NOT NULL) no banco - insistir em gravar
    @Column(name = "nome", nullable = false)
    private String nome;

    // foreign keys
    @Column(name = "projeto_id")
    private Integer projetoId;

    @Column(name = "sprint_id")
    private Integer sprintId;

    private String project;

    private String sprint;

    @Column(name = "atribuido_para")
    private Integer assignee;

    private String avatar;

    private String avatarColor;

    private String priority;

    private String status;

    @Column(name = "prioridade_id")
    private Integer prioridadeId;

    @Column(name = "status_id")
    private Integer statusId;

    private Integer daysDelayed;

    private LocalDate plannedEnd;

    private Integer estimatedHours;

    private String blockedBy;

    @Column(columnDefinition = "TEXT")
    private String tags; // JSON array

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
}
