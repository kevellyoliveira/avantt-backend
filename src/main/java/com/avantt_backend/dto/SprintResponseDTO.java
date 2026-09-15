package com.avantt_backend.dto;

import java.time.LocalDate;
import java.util.List;

public class SprintResponseDTO {
    private Integer id;
    private String name;
    private String project;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private int daysDelayed;
    private int progress;
    private int totalTasks;
    private int doneTasks;
    private int blockedTasks;
    private List<String> team;
    private String goal;

    public SprintResponseDTO() {}

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getDaysDelayed() { return daysDelayed; }
    public void setDaysDelayed(int daysDelayed) { this.daysDelayed = daysDelayed; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getDoneTasks() { return doneTasks; }
    public void setDoneTasks(int doneTasks) { this.doneTasks = doneTasks; }

    public int getBlockedTasks() { return blockedTasks; }
    public void setBlockedTasks(int blockedTasks) { this.blockedTasks = blockedTasks; }

    public List<String> getTeam() { return team; }
    public void setTeam(List<String> team) { this.team = team; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}
