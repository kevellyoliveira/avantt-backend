package com.avantt_backend.repository;

import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoUsuarioRepository extends JpaRepository<ProjetoUsuario, ProjetoUsuarioId> {
    List<ProjetoUsuario> findByIdProjetoId(Integer projetoId);
    List<ProjetoUsuario> findByIdUsuarioId(Integer usuarioId);
    List<ProjetoUsuario> findByIdProjetoIdIn(List<Integer> projetoIds);
}
