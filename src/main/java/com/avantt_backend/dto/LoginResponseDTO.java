
package com.avantt_backend.dto;

public class LoginResponseDTO {

    private String token;
    private UsuarioResponseDTO user;
    private boolean requiresFirstAccessValidation;
    private String email;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(
            String token,
            UsuarioResponseDTO user,
            boolean requiresFirstAccessValidation,
            String email) {

        this.token = token;
        this.user = user;
        this.requiresFirstAccessValidation = requiresFirstAccessValidation;
        this.email = email;
    }

    public static LoginResponseDTO primeiroAcessoPendente(String email) {
        return new LoginResponseDTO(
                null,
                null,
                true,
                email
        );
    }

    public static LoginResponseDTO loginConcluido(
            String token,
            UsuarioResponseDTO user) {

        return new LoginResponseDTO(
                token,
                user,
                false,
                user == null ? null : user.getEmail()
        );
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuarioResponseDTO getUser() {
        return user;
    }

    public void setUser(UsuarioResponseDTO user) {
        this.user = user;
    }

    public boolean isRequiresFirstAccessValidation() {
        return requiresFirstAccessValidation;
    }

    public void setRequiresFirstAccessValidation(
            boolean requiresFirstAccessValidation) {
        this.requiresFirstAccessValidation = requiresFirstAccessValidation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
