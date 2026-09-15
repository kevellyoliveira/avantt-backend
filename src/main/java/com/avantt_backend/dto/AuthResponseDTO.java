package com.avantt_backend.dto;

public class AuthResponseDTO {
    private String token;
    private UsuarioResponseDTO user;

    public AuthResponseDTO() {}
    public AuthResponseDTO(String token, UsuarioResponseDTO user) { this.token = token; this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UsuarioResponseDTO getUser() { return user; }
    public void setUser(UsuarioResponseDTO user) { this.user = user; }
}
