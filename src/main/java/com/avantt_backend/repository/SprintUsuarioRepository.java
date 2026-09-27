package com.avantt_backend.repository;

import com.avantt_backend.entity.SprintUsuario;
import com.avantt_backend.entity.SprintUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintUsuarioRepository extends JpaRepository<SprintUsuario, SprintUsuarioId> {
    List<SprintUsuario> findByIdSprintId(Integer sprintId);
    List<SprintUsuario> findByIdUsuarioId(Integer usuarioId);
}
