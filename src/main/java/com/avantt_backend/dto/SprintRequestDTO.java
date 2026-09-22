package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class SprintRequestDTO {
    private String name;
    private String project;

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
    private List<String> team;
    private String goal;

    public SprintRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }

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

    public List<String> getTeam() { return team; }
    public void setTeam(List<String> team) { this.team = team; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}
