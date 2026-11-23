package com.avantt_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarefa")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Colunas existentes na tabela tarefa

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "projeto_id", nullable = false)
    private Integer projetoId;

    @Column(name = "sprint_id")
    private Integer sprintId;

    @Column(name = "status_id", nullable = false)
    private Integer statusId;

    @Column(name = "prioridade_id", nullable = false)
    private Integer prioridadeId;

    @Column(name = "tarefa_pai_id")
    private Integer tarefaPaiId;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "horas_estimadas")
    private Integer estimatedHours;

    @Column(name = "is_subtarefa")
    private Boolean isSubtarefa;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate plannedEnd;

    @Column(name = "data_criacao", insertable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", insertable = false, updatable = false)
    private LocalDateTime dataAtualizacao;

    // Campos auxiliares: não existem como colunas na tabela tarefa.
    // São mantidos para compatibilidade com o service e os DTOs.

    @Transient
    private String nome;

    @Transient
    private String project;

    @Transient
    private String sprint;

    @Column(name = "atribuido_para")
    private Integer assignee;

    @Transient
    private String avatar;

    @Transient
    private String avatarColor;

    @Transient
    private String priority;

    @Transient
    private String status;

    @Transient
    private Integer daysDelayed;

    @Transient
    private String blockedBy;

    @Transient
    private String tags;

    public Tarefa() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(Integer projetoId) {
        this.projetoId = projetoId;
    }

    public Integer getSprintId() {
        return sprintId;
    }

    public void setSprintId(Integer sprintId) {
        this.sprintId = sprintId;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getSprint() {
        return sprint;
    }

    public void setSprint(String sprint) {
        this.sprint = sprint;
    }

    public Integer getAssignee() {
        return assignee;
    }

    public void setAssignee(Integer assignee) {
        this.assignee = assignee;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getPrioridadeId() {
        return prioridadeId;
    }

    public void setPrioridadeId(Integer prioridadeId) {
        this.prioridadeId = prioridadeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getDaysDelayed() {
        return daysDelayed;
    }

    public void setDaysDelayed(Integer daysDelayed) {
        this.daysDelayed = daysDelayed;
    }

    public LocalDate getPlannedEnd() {
        return plannedEnd;
    }

    public void setPlannedEnd(LocalDate plannedEnd) {
        this.plannedEnd = plannedEnd;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public String getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(String blockedBy) {
        this.blockedBy = blockedBy;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getTarefaPaiId() {
        return tarefaPaiId;
    }

    public void setTarefaPaiId(Integer tarefaPaiId) {
        this.tarefaPaiId = tarefaPaiId;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    public Boolean getIsSubtarefa() {
        return isSubtarefa;
    }

    public void setIsSubtarefa(Boolean isSubtarefa) {
        this.isSubtarefa = isSubtarefa;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}