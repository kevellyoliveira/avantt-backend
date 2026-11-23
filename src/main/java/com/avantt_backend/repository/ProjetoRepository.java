
package com.avantt_backend.repository;

import com.avantt_backend.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Integer> {

    Optional<Projeto> findByName(String name);

    Optional<Projeto> findByIdAndOrganizacaoId(
            Integer id,
            Integer organizacaoId
    );

    boolean existsByIdAndOrganizacaoId(
            Integer id,
            Integer organizacaoId
    );

    List<Projeto> findByEndDate(LocalDate endDate);
}
