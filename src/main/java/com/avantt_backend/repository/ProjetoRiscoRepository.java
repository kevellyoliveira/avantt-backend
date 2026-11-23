
package com.avantt_backend.repository;

import com.avantt_backend.entity.ProjetoRisco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoRiscoRepository
        extends JpaRepository<ProjetoRisco, Integer> {

    List<ProjetoRisco> findByProjeto_Id(Integer projetoId);

    void deleteByProjeto_Id(Integer projetoId);
}
