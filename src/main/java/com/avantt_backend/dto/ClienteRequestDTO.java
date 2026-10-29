package com.avantt_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * DTO de requisição vindo do frontend. Usa nomes compatíveis com o frontend (name, document, email, phone).
 */
public class ClienteRequestDTO {

    @Positive(message = "id inválido")
    private Integer id;

    @NotBlank(message = "name é obrigatório")
    @JsonAlias({"nome", "Name"})
    private String name;

    // documento/CNPJ opcional (frontend envia formatado)
    @JsonAlias({"documento", "document"})
    private String document;

    @Email(message = "email inválido")
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;

    @JsonAlias({"usuario_gestor", "defaultUser", "default_user"})
    private DefaultUser defaultUser;

    private Boolean isAtivo;

    public ClienteRequestDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public DefaultUser getDefaultUser() { return defaultUser; }
    public void setDefaultUser(DefaultUser defaultUser) { this.defaultUser = defaultUser; }

    public Boolean getIsAtivo() { return isAtivo; }
    public void setIsAtivo(Boolean isAtivo) { this.isAtivo = isAtivo; }

    public static class DefaultUser {
        @JsonAlias({"nome", "name"})
        private String name;
        @JsonAlias({"email"})
        private String email;
        @JsonAlias({"status","is_ativo"})
        private String status;
        @JsonAlias({"role","papel"})
        private String role;
        @JsonAlias({"projectIds","projeto_ids","project_ids"})
        private List<String> projectIds;

        public DefaultUser() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public List<String> getProjectIds() { return projectIds; }
        public void setProjectIds(List<String> projectIds) { this.projectIds = projectIds; }
    }
}
