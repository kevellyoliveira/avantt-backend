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

    private String status;

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

    private List<String> team;

    private List<String> risks;

    private List<MilestoneDTO> milestones;

    public ProjetoRequestDTO() {}

    // getters and setters
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

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public SprintsDTO getSprints() { return sprints; }
    public void setSprints(SprintsDTO sprints) { this.sprints = sprints; }

    public ProjectTasksDTO getTasks() { return tasks; }
    public void setTasks(ProjectTasksDTO tasks) { this.tasks = tasks; }

    public List<String> getTeam() { return team; }
    public void setTeam(List<String> team) { this.team = team; }

    public List<String> getRisks() { return risks; }
    public void setRisks(List<String> risks) { this.risks = risks; }

    public List<MilestoneDTO> getMilestones() { return milestones; }
    public void setMilestones(List<MilestoneDTO> milestones) { this.milestones = milestones; }
}
