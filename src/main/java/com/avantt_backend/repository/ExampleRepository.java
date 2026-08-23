package com.avantt_backend.repository;

import com.avantt_backend.model.ExampleModel;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA para ExampleModel.
 */
public interface ExampleRepository extends JpaRepository<ExampleModel, Long> {
}
