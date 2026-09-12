package com.avantt_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sprint")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    private String project; // project name or id

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer daysDelayed;

    private Integer progress;

    private Integer totalTasks;

    private Integer doneTasks;

    private Integer blockedTasks;

    @Column(columnDefinition = "TEXT")
    private String team; // JSON array

    @Column(columnDefinition = "TEXT")
    private String goal;

    public Sprint() {}

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

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

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}
