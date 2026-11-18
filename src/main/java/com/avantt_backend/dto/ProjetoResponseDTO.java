
package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class ProjetoResponseDTO {

    private String id;

    private String name;

    private String description;

    private String color;

    private String status;

    private Integer statusId;

    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private int progress;

    private SprintsDTO sprints;

    private ProjectTasksDTO tasks;

    private List<String> team;

    // IDs dos integrantes da equipe
    private List<Integer> teamIds;

    // Marcos do projeto
    private List<ProjetoMarcoDTO> milestones;

    // Riscos do projeto
    private List<String> risks;

    private Integer clienteId;

    private Integer organizacaoId;

    private String organizacaoName;

    public ProjetoResponseDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public SprintsDTO getSprints() {
        return sprints;
    }

    public void setSprints(SprintsDTO sprints) {
        this.sprints = sprints;
    }

    public ProjectTasksDTO getTasks() {
        return tasks;
    }

    public void setTasks(ProjectTasksDTO tasks) {
        this.tasks = tasks;
    }

    public List<String> getTeam() {
        return team;
    }

    public void setTeam(List<String> team) {
        this.team = team;
    }

    public List<Integer> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(List<Integer> teamIds) {
        this.teamIds = teamIds;
    }

    public List<ProjetoMarcoDTO> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<ProjetoMarcoDTO> milestones) {
        this.milestones = milestones;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
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

    public String getOrganizacaoName() {
        return organizacaoName;
    }

    public void setOrganizacaoName(String organizacaoName) {
        this.organizacaoName = organizacaoName;
    }
}
