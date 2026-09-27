package com.avantt_backend.repository;

import com.avantt_backend.entity.TarefaTag;
import com.avantt_backend.entity.TarefaTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaTagRepository extends JpaRepository<TarefaTag, TarefaTagId> {
    List<TarefaTag> findByIdTarefaId(Integer tarefaId);
    void deleteByIdTarefaId(Integer tarefaId);
}
