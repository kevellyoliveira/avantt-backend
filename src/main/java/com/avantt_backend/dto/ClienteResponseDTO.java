package com.avantt_backend.dto;

import java.time.LocalDateTime;

public class ClienteResponseDTO {
    private String id;
    private String name;
    private String document;
    private String email;
    private String phone;
    private DefaultUser defaultUser;
    private Boolean isAtivo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataDesativacao;

    public ClienteResponseDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public LocalDateTime getDataDesativacao() { return dataDesativacao; }
    public void setDataDesativacao(LocalDateTime dataDesativacao) { this.dataDesativacao = dataDesativacao; }

    public static class DefaultUser {
        private String name;
        private String email;
        private String status;
        private String role;
        private java.util.List<String> projectIds;

        public DefaultUser() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public java.util.List<String> getProjectIds() { return projectIds; }
        public void setProjectIds(java.util.List<String> projectIds) { this.projectIds = projectIds; }
    }
}
