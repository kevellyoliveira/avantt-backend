package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class SprintRequestDTO {
    private String name;
    private Integer projectId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Data de início deve ser uma data futura ou presente")
    @NotNull(message = "Data de início é obrigatória")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Data de fim deve ser uma data futura")
    @NotNull(message = "Data de fim é obrigatória")
    private LocalDate endDate;

    private Integer daysDelayed;
    private Integer progress;
    private Integer totalTasks;
    private Integer doneTasks;
    private Integer blockedTasks;
    private List<Integer> team;
    private List<Integer> addTeam;
    private List<Integer> removeTeam;
    private Integer statusId;

    public SprintRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getDaysDelayed() { return daysDelayed; }
    public void setDaysDelayed(Integer daysDelayed) { this.daysDelayed = daysDelayed; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Integer getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Integer totalTasks) { this.totalTasks = totalTasks; }

    public Integer getDoneTasks() { return doneTasks; }
    public void setDoneTasks(Integer doneTasks) { this.doneTasks = doneTasks; }

    public Integer getBlockedTasks() { return blockedTasks; }
    public void setBlockedTasks(Integer blockedTasks) { this.blockedTasks = blockedTasks; }

    public List<Integer> getTeam() { return team; }
    public void setTeam(List<Integer> team) { this.team = team; }

    public List<Integer> getAddTeam() { return addTeam; }
    public void setAddTeam(List<Integer> addTeam) { this.addTeam = addTeam; }

    public List<Integer> getRemoveTeam() { return removeTeam; }
    public void setRemoveTeam(List<Integer> removeTeam) { this.removeTeam = removeTeam; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }
}
