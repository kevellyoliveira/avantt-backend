package com.avantt_backend.dto;

public class ClienteResponseDTO {
    private Integer id;
    private Integer organizationId;
    private Integer clientOrganizationId;
    private String name;
    private String document;
    private String email;
    private String phone;
    private String createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
    public Integer getClientOrganizationId() { return clientOrganizationId; }
    public void setClientOrganizationId(Integer clientOrganizationId) { this.clientOrganizationId = clientOrganizationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
