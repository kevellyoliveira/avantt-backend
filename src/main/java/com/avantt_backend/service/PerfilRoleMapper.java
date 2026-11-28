package com.avantt_backend.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

final class PerfilRoleMapper {

    private PerfilRoleMapper() {
    }

    static Optional<String> fromProfileName(String profileName) {
        if (profileName == null || profileName.isBlank()) {
            return Optional.empty();
        }

        String normalized = Normalizer.normalize(
                profileName,
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);

        if (normalized.contains("gerente")
                || normalized.contains("gestor")
                || normalized.contains("admin")
                || normalized.contains("manager")) {
            return Optional.of("manager");
        }

        if (normalized.contains("desenvol")
                || normalized.contains("colaborador")
                || normalized.contains("developer")) {
            return Optional.of("developer");
        }

        return Optional.empty();
    }
}
