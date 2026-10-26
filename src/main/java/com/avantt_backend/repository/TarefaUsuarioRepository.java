package com.avantt_backend.repository;

import com.avantt_backend.entity.TarefaUsuario;
import com.avantt_backend.entity.TarefaUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarefaUsuarioRepository extends JpaRepository<TarefaUsuario, TarefaUsuarioId> {
    List<TarefaUsuario> findByIdTarefaId(Integer tarefaId);
}
