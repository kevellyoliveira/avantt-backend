package com.avantt_backend.repository;

import com.avantt_backend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByOrganizacaoId(Integer organizacaoId);

    Optional<Cliente> findByIdAndOrganizacaoId(Integer id, Integer organizacaoId);

    boolean existsByIdAndOrganizacaoId(Integer id, Integer organizacaoId);
}
