
package com.avantt_backend.dto;

import com.avantt_backend.entity.Usuario;

public class AuthResponseDTO {

    private String token;
    private UsuarioResponseDTO user;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, UsuarioResponseDTO user) {
        this.token = token;
        this.user = user;
    }

    public AuthResponseDTO(String authToken, Usuario usuarioSalvo) {
        this.token = authToken;

        UsuarioResponseDTO usuarioDTO = new UsuarioResponseDTO();

        usuarioDTO.setId(String.valueOf(usuarioSalvo.getId()));
        usuarioDTO.setName(usuarioSalvo.getNome());
        usuarioDTO.setEmail(usuarioSalvo.getEmail());
        usuarioDTO.setRole(usuarioSalvo.getCargo());
        usuarioDTO.setAvatar(gerarIniciais(usuarioSalvo.getNome()));
        usuarioDTO.setColor("#2563eb");
        usuarioDTO.setOrganizacaoId(usuarioSalvo.getOrganizacaoId());

        this.user = usuarioDTO;
    }

    private String gerarIniciais(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return "";
        }

        String[] partes = nome.trim().split("\\s+");

        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }

        return (
                partes[0].substring(0, 1)
                        + partes[partes.length - 1].substring(0, 1)
        ).toUpperCase();
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
}
