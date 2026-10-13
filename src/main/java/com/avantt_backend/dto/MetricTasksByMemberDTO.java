package com.avantt_backend.dto;

public class MetricTasksByMemberDTO {
    private Integer usuarioId;
    private String usuarioNome;
    private Integer projetoId;
    private Integer tarefas;
    private Double horasEstimadas;

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
    public Integer getProjetoId() { return projetoId; }
    public void setProjetoId(Integer projetoId) { this.projetoId = projetoId; }
    public Integer getTarefas() { return tarefas; }
    public void setTarefas(Integer tarefas) { this.tarefas = tarefas; }
    public Double getHorasEstimadas() { return horasEstimadas; }
    public void setHorasEstimadas(Double horasEstimadas) { this.horasEstimadas = horasEstimadas; }
}
