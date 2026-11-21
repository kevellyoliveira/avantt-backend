package com.avantt_backend.util;

public final class StatusUtils {

    private StatusUtils() {}

    // Normalize various status inputs to the canonical project statuses in Portuguese
    // Returns one of: "Planejado", "Em andamento", "Concluído", "Cancelado", or null if input is null/blank
    public static String normalizeProjectStatus(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.contains("planej") || s.equals("planned")) return "Planejado";
        if (s.contains("andam") || s.contains("in progress") || s.equals("inprogress") || s.equals("ongoing")) return "Em andamento";
        if (s.contains("concl") || s.contains("done") || s.contains("feito") || s.contains("completed")) return "Concluído";
        if (s.contains("cancel")) return "Cancelado";
        // fallback: capitalize first letter
        return capitalize(raw);
    }

    public static boolean isDone(String raw) {
        String n = normalizeProjectStatus(raw);
        return n != null && n.equals("Concluído");
    }

    public static boolean isCancelled(String raw) {
        String n = normalizeProjectStatus(raw);
        return n != null && n.equals("Cancelado");
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        String t = s.trim();
        return t.substring(0,1).toUpperCase() + (t.length() > 1 ? t.substring(1) : "");
    }

    // Normalize task status to the canonical labels used by the frontend.
    public static String normalizeTaskStatus(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.contains("to do") || s.contains("todo") || s.contains("planej")) return "planejada";
        if (s.contains("in progress") || s.contains("inprogress") || s.contains("andam")) return "em andamento";
        if (s.contains("bloque") || s.contains("block")) return "bloqueada";
        if (s.contains("revis")) return "revisão";
        if (s.contains("done") || s.contains("concl")) return "concluída";
        if (s.contains("cancel")) return "cancelada";
        return s;
    }

    public static boolean isValidTaskStatus(String raw) {
        String normalized = normalizeTaskStatus(raw);
        return "planejada".equals(normalized)
                || "em andamento".equals(normalized)
                || "concluída".equals(normalized)
                || "bloqueada".equals(normalized)
                || "revisão".equals(normalized)
                || "cancelada".equals(normalized);
    }

    public static String normalizePriority(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.contains("baixa")) return "baixa";
        if (s.contains("média") || s.contains("media") || s.equals("medio")) return "média";
        if (s.contains("alta")) return "alta";
        if (s.contains("crít") || s.contains("crit")) return "crítica";
        return s;
    }
}
