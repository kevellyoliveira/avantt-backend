package com.avantt_backend.dto;

import java.util.List;

public class UsuarioResponseDTO {
    private String id;
    private String name;
    private String avatar;
    private String color;
    private String role;
    private TasksDTO tasks;
    private List<String> projects;
    private List<String> projectIds;
    private int workload;
    private String email;
    private Integer organizationId;
    private String organizationName;

    public UsuarioResponseDTO() {}

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public TasksDTO getTasks() { return tasks; }
    public void setTasks(TasksDTO tasks) { this.tasks = tasks; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public List<String> getProjectIds() { return projectIds; }
    public void setProjectIds(List<String> projectIds) { this.projectIds = projectIds; }

    public int getWorkload() { return workload; }
    public void setWorkload(int workload) { this.workload = workload; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
}
