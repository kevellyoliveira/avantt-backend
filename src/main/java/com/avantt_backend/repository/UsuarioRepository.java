
package com.avantt_backend.repository;

import com.avantt_backend.entity.Usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    boolean existsByEmail(String email);

    Optional<Usuario> findByNomeIgnoreCase(String nome);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByAuthToken(String token);

    Optional<Usuario> findByResetToken(String token);

    Optional<Usuario> findByConviteTokenHash(String hash);
}
