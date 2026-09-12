package com.avantt_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "projeto")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String color;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer progress;

    @Column(columnDefinition = "TEXT")
    private String sprints; // stored as JSON

    @Column(columnDefinition = "TEXT")
    private String tasks; // stored as JSON

    @Column(columnDefinition = "TEXT")
    private String team; // JSON array

    @Column(columnDefinition = "TEXT")
    private String risks; // JSON array

    @Column(columnDefinition = "TEXT")
    private String milestones; // JSON array

    public Projeto() {}

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

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

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getSprints() { return sprints; }
    public void setSprints(String sprints) { this.sprints = sprints; }

    public String getTasks() { return tasks; }
    public void setTasks(String tasks) { this.tasks = tasks; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getRisks() { return risks; }
    public void setRisks(String risks) { this.risks = risks; }

    public String getMilestones() { return milestones; }
    public void setMilestones(String milestones) { this.milestones = milestones; }
}
