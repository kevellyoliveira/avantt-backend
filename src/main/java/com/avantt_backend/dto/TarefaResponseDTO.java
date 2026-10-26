package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public class TarefaResponseDTO {
    private String id;
    private String title;
    private String project;
    private String sprint;
    private String assignee;
    private String avatar;
    private String avatarColor;
    private String priority;
    private String status;
    private Integer statusId;
    private String statusName;
    private int daysDelayed;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedEnd;

    private Integer estimatedHours;
    private String blockedBy;
    private String description;
    private List<Integer> tagIds;
    private Integer sprintProgress;

    public TarefaResponseDTO() {}

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }

    public String getSprint() { return sprint; }
    public void setSprint(String sprint) { this.sprint = sprint; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public int getDaysDelayed() { return daysDelayed; }
    public void setDaysDelayed(int daysDelayed) { this.daysDelayed = daysDelayed; }

    public LocalDate getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getBlockedBy() { return blockedBy; }
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Integer> getTagIds() { return tagIds; }
    public void setTagIds(List<Integer> tagIds) { this.tagIds = tagIds; }

    public Integer getSprintProgress() { return sprintProgress; }
    public void setSprintProgress(Integer sprintProgress) { this.sprintProgress = sprintProgress; }
}
