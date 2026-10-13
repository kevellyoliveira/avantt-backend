package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class TarefaRequestDTO {

    @NotBlank(message = "Titulo é obrigatório")
    private String title;

    @NotNull(message = "projectId é obrigatório")
    private Integer projectId;

    @NotNull(message = "sprintId é obrigatório")
    private Integer sprintId;
    // front expects `assignee` (user id). This maps to tarefa.atribuido_para in DB
    private Integer assigneeId;
    private String avatar;
    private String avatarColor;
    private String priority;
    @NotNull(message = "statusId é obrigatório")
    private Integer statusId;
    private Integer prioridadeId;
    private Integer daysDelayed;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Data de entrega é obrigatório")
    @Future(message = "Data de entrega deve ser no futuro")
    private LocalDate plannedEnd;

    private Integer estimatedHours;
    private String blockedBy;
    private List<Integer> tagIds;
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String description;

    public TarefaRequestDTO() {}

    // getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public Integer getSprintId() { return sprintId; }
    public void setSprintId(Integer sprintId) { this.sprintId = sprintId; }

    public Integer getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public Integer getPrioridadeId() { return prioridadeId; }
    public void setPrioridadeId(Integer prioridadeId) { this.prioridadeId = prioridadeId; }

    public Integer getDaysDelayed() { return daysDelayed; }
    public void setDaysDelayed(Integer daysDelayed) { this.daysDelayed = daysDelayed; }

    public LocalDate getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(LocalDate plannedEnd) { this.plannedEnd = plannedEnd; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getBlockedBy() { return blockedBy; }
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }

    public List<Integer> getTagIds() { return tagIds; }
    public void setTagIds(List<Integer> tagIds) { this.tagIds = tagIds; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
