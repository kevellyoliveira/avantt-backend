package com.avantt_backend.repository;

import com.avantt_backend.entity.MarcoProjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarcoProjetoRepository extends JpaRepository<MarcoProjeto, Integer> {
    List<MarcoProjeto> findByProjetoId(Integer projetoId);
    List<MarcoProjeto> findByProjetoIdIn(List<Integer> projetoIds);
}
