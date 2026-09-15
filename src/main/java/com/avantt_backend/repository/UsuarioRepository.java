package com.avantt_backend.repository;

import com.avantt_backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByEmail(String email);
    java.util.Optional<Usuario> findByNomeIgnoreCase(String nome);
    java.util.Optional<Usuario> findByEmail(String email);
    java.util.Optional<Usuario> findByAuthToken(String token);
    java.util.Optional<Usuario> findByResetToken(String token);
}
