package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public class ProjetoRequestDTO {

    @NotBlank(message = "name é obrigatório")
    private String name;

    private String description;

    private String color;

    @NotNull(message = "statusId é obrigatório")
    private Integer statusId;

    @NotNull(message = "Data de início é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Data de início deve ser uma data futura ou presente")
    private LocalDate startDate;

    @NotNull(message = "Data de fim é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Data de fim deve ser uma data futura")
    private LocalDate endDate;

    private int progress;

    private SprintsDTO sprints;

    private ProjectTasksDTO tasks;

    private List<Integer> team;
    private List<Integer> addTeam;
    private List<Integer> removeTeam;

    // relacionamentos
    private Integer clienteId;
    private Integer organizacaoId;

    public ProjetoRequestDTO() {}

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public SprintsDTO getSprints() { return sprints; }
    public void setSprints(SprintsDTO sprints) { this.sprints = sprints; }

    public ProjectTasksDTO getTasks() { return tasks; }
    public void setTasks(ProjectTasksDTO tasks) { this.tasks = tasks; }

    public List<Integer> getTeam() { return team; }
    public void setTeam(List<Integer> team) { this.team = team; }

    public List<Integer> getAddTeam() { return addTeam; }
    public void setAddTeam(List<Integer> addTeam) { this.addTeam = addTeam; }

    public List<Integer> getRemoveTeam() { return removeTeam; }
    public void setRemoveTeam(List<Integer> removeTeam) { this.removeTeam = removeTeam; }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public Integer getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(Integer organizacaoId) { this.organizacaoId = organizacaoId; }


}
