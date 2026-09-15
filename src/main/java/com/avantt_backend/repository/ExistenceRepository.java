package com.avantt_backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class ExistenceRepository {

    @PersistenceContext
    private EntityManager em;

    public boolean existsPerfilById(Integer perfilId) {
        if (perfilId == null) return false;
        Object single = em.createNativeQuery("select 1 from perfil where id = :id")
                .setParameter("id", perfilId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst().orElse(null);
        return single != null;
    }

    public boolean existsOrganizacaoById(Integer organizacaoId) {
        if (organizacaoId == null) return false;
        Object single = em.createNativeQuery("select 1 from organizacao where id = :id")
                .setParameter("id", organizacaoId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst().orElse(null);
        return single != null;
    }
}
