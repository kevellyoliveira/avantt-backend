package com.avantt_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UsuarioRequestDTO {

    @NotBlank(message = "name é obrigatório")
    private String name;

    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    private String email;

    private String role;
    private String avatar;
    private String color;
    private List<String> projects;
    private TasksDTO tasks;
    private Integer workload;

    public UsuarioRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public TasksDTO getTasks() { return tasks; }
    public void setTasks(TasksDTO tasks) { this.tasks = tasks; }

    public Integer getWorkload() { return workload; }
    public void setWorkload(Integer workload) { this.workload = workload; }
}
