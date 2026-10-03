package com.avantt_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para cálculo de estimativas.
 */
public class EstimativaRequestDTO {

    @NotNull
    @Min(1)
    private Integer pessoas;

    @NotNull
    @Min(1)
    private Integer sprints;

    @Min(value = 15, message = "Quantidade mínima de dias por sprint é 15")
    private Integer quantidadeDiasSprint = 15; // em dias, padrão 15

    private Integer prazo; // opcional, em minutos

    

    @Min(1)
    // limiteTempo agora é em minutos (p.ex. 144000 = 300 dias * 480 minutos/dia)
    private int limiteTempo = 144000;

    @Min(1)
    private int limitePessoas = 30;

    @Min(1)
    private int limiteSprints = 15;

    public EstimativaRequestDTO() {
    }

    public Integer getPessoas() {
        return pessoas;
    }

    public void setPessoas(Integer pessoas) {
        this.pessoas = pessoas;
    }

    public Integer getSprints() {
        return sprints;
    }

    public void setSprints(Integer sprints) {
        this.sprints = sprints;
    }

    public Integer getQuantidadeDiasSprint() {
        return quantidadeDiasSprint;
    }

    public void setQuantidadeDiasSprint(Integer quantidadeDiasSprint) {
        this.quantidadeDiasSprint = quantidadeDiasSprint;
    }

    public Integer getPrazo() {
        return prazo;
    }

    public void setPrazo(Integer prazo) {
        this.prazo = prazo;
    }

    

    public int getLimiteTempo() {
        return limiteTempo;
    }

    public void setLimiteTempo(int limiteTempo) {
        this.limiteTempo = limiteTempo;
    }

    public int getLimitePessoas() {
        return limitePessoas;
    }

    public void setLimitePessoas(int limitePessoas) {
        this.limitePessoas = limitePessoas;
    }

    public int getLimiteSprints() {
        return limiteSprints;
    }

    public void setLimiteSprints(int limiteSprints) {
        this.limiteSprints = limiteSprints;
    }
}
