package com.avantt_backend.repository;

import com.avantt_backend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    java.util.List<Cliente> findByOrganizacaoId(Integer organizacaoId);
    boolean existsByIdAndOrganizacaoId(Integer id, Integer organizacaoId);
    boolean existsByOrganizacaoIdAndNomeIgnoreCase(Integer organizacaoId, String nome);
    boolean existsByOrganizacaoIdAndDocumento(Integer organizacaoId, String documento);
    boolean existsByOrganizacaoIdAndEmailIgnoreCase(Integer organizacaoId, String email);
    boolean existsByOrganizacaoIdAndTelefone(Integer organizacaoId, String telefone);
}
