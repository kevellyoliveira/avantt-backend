package com.avantt_backend.dto;

import com.avantt_backend.entity.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthResponseDTOTest {

    @Test
    void usesSystemRoleForFirstAccessSession() {
        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setNome("Ana");
        usuario.setEmail("ana@example.com");
        usuario.setCargo("QA");
        usuario.setPapel("developer");

        AuthResponseDTO response = new AuthResponseDTO("token", usuario);

        assertEquals("developer", response.getUser().getRole());
        assertEquals("developer", response.getUser().getPapel());
    }
}
