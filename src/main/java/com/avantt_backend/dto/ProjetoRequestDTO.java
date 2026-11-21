
package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class ProjetoRequestDTO {

    @NotBlank(message = "name é obrigatório")
    private String name;

    private String description;

    private String color;

    // Status do projeto: Planejada, Em andamento,
    // Concluída ou Bloqueada.
    private String status;

    // Mantido temporariamente para compatibilidade
    // com possíveis utilizações antigas.
    private Integer statusId;

    @NotNull(message = "Data de início é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Data de início deve ser uma data futura ou presente")
    private LocalDate startDate;

    @NotNull(message = "Data de fim é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Data de fim deve ser uma data futura")
    private LocalDate endDate;

    private Integer progress;

    private SprintsDTO sprints;

    private ProjectTasksDTO tasks;

    private List<Integer> team;

    private List<Integer> addTeam;

    private List<Integer> removeTeam;

    // Marcos do projeto
    @Valid
    private List<ProjetoMarcoDTO> milestones;

    // Riscos do projeto
    private List<String> risks;

    // Relacionamentos
    @JsonAlias("clientId")
    private Integer clienteId;
    private boolean clienteIdProvided;

    private Integer organizacaoId;

    public ProjetoRequestDTO() {
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

    public List<Integer> getTeam() {
        return team;
    }

    public void setTeam(List<Integer> team) {
        this.team = team;
    }

    public List<Integer> getAddTeam() {
        return addTeam;
    }

    public void setAddTeam(List<Integer> addTeam) {
        this.addTeam = addTeam;
    }

    public List<Integer> getRemoveTeam() {
        return removeTeam;
    }

    public void setRemoveTeam(List<Integer> removeTeam) {
        this.removeTeam = removeTeam;
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
        this.clienteIdProvided = true;
    }

    public boolean isClienteIdProvided() {
        return clienteIdProvided;
    }

    public Integer getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(Integer organizacaoId) {
        this.organizacaoId = organizacaoId;
    }
}
