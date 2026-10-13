package com.avantt_backend.dto;

public class MetricOverdueTaskDTO {
    private Integer tarefaId;
    private Integer projetoId;
    private Integer sprintId;
    private String titulo;
    private String dataFim;
    private String status;
    private Integer diasAtraso;

    public Integer getTarefaId() { return tarefaId; }
    public void setTarefaId(Integer tarefaId) { this.tarefaId = tarefaId; }
    public Integer getProjetoId() { return projetoId; }
    public void setProjetoId(Integer projetoId) { this.projetoId = projetoId; }
    public Integer getSprintId() { return sprintId; }
    public void setSprintId(Integer sprintId) { this.sprintId = sprintId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDiasAtraso() { return diasAtraso; }
    public void setDiasAtraso(Integer diasAtraso) { this.diasAtraso = diasAtraso; }
}
