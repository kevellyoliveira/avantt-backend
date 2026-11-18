
package com.avantt_backend.repository;

import com.avantt_backend.entity.ProjetoMarco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoMarcoRepository
        extends JpaRepository<ProjetoMarco, Integer> {

    List<ProjetoMarco> findByProjeto_IdOrderByDateAsc(Integer projetoId);

    void deleteByProjeto_Id(Integer projetoId);
}
