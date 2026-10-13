package com.avantt_backend.dto;

import java.util.List;

/**
 * Resposta simples do cálculo de estimativas (modelo original).
 * Campos tempoEstimado e tempoRestante estão em dias.
 */
public class EstimativaResponseDTO {

    private double tempoEstimado;
    private double tempoRestante;
    private boolean dentroPrazo;
    private List<String> pessoasCenarios;
    private List<String> sprintsCenarios;

    private int melhorPessoas;
    private int melhorSprints;
    private double melhorTempo;
    private String melhorMensagem; // mensagem explicativa sobre o melhorTempo (unidade/semântica)

    public EstimativaResponseDTO() {
    }

    public double getTempoEstimado() {
        return tempoEstimado;
    }

    public void setTempoEstimado(double tempoEstimado) {
        this.tempoEstimado = tempoEstimado;
    }

    public double getTempoRestante() {
        return tempoRestante;
    }

    public void setTempoRestante(double tempoRestante) {
        this.tempoRestante = tempoRestante;
    }

    public boolean isDentroPrazo() {
        return dentroPrazo;
    }

    public void setDentroPrazo(boolean dentroPrazo) {
        this.dentroPrazo = dentroPrazo;
    }

    public List<String> getPessoasCenarios() {
        return pessoasCenarios;
    }

    public void setPessoasCenarios(List<String> pessoasCenarios) {
        this.pessoasCenarios = pessoasCenarios;
    }

    public List<String> getSprintsCenarios() {
        return sprintsCenarios;
    }

    public void setSprintsCenarios(List<String> sprintsCenarios) {
        this.sprintsCenarios = sprintsCenarios;
    }

    public int getMelhorPessoas() {
        return melhorPessoas;
    }

    public void setMelhorPessoas(int melhorPessoas) {
        this.melhorPessoas = melhorPessoas;
    }

    public int getMelhorSprints() {
        return melhorSprints;
    }

    public void setMelhorSprints(int melhorSprints) {
        this.melhorSprints = melhorSprints;
    }

    public double getMelhorTempo() {
        return melhorTempo;
    }

    public void setMelhorTempo(double melhorTempo) {
        this.melhorTempo = melhorTempo;
    }

    public String getMelhorMensagem() {
        return melhorMensagem;
    }

    public void setMelhorMensagem(String melhorMensagem) {
        this.melhorMensagem = melhorMensagem;
    }
}
