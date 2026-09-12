package com.avantt_backend.repository;

import com.avantt_backend.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Integer> {

    java.util.Optional<com.avantt_backend.entity.Projeto> findByName(String name);

}
