package com.avantt_backend.dto;

public class MetricCompletionRateDTO {
    private Integer projetoId;
    private Integer concluidas;
    private Integer total;
    private Double pctConclusao;

    public Integer getProjetoId() { return projetoId; }
    public void setProjetoId(Integer projetoId) { this.projetoId = projetoId; }
    public Integer getConcluidas() { return concluidas; }
    public void setConcluidas(Integer concluidas) { this.concluidas = concluidas; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Double getPctConclusao() { return pctConclusao; }
    public void setPctConclusao(Double pctConclusao) { this.pctConclusao = pctConclusao; }
}
