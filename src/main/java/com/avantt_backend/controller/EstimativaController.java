package com.avantt_backend.controller;

import com.avantt_backend.dto.EstimativaRequestDTO;
import com.avantt_backend.dto.EstimativaResponseDTO;
import com.avantt_backend.util.ApiPaths;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.avantt_backend.dto.ErrorResponse;

/**
 * Controller que expõe a lógica de estimativas traduzida do notebook do usuário.
 */
@RestController
@RequestMapping(ApiPaths.ESTIMATIVAS)
public class EstimativaController {

    private static final int DIAS_POR_SPRINT = 15;

    @PostMapping(value = "/calc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> calcular(@Valid @RequestBody EstimativaRequestDTO req) {

        // validação contra limites: pessoas, sprints e prazo relativo ao limiteTempo
        Map<String, String> errors = new HashMap<>();
        if (req.getPessoas() > req.getLimitePessoas()) {
            errors.put("pessoas", String.format("Quantidade de pessoas (%d) excede o limite permitido (%d)", req.getPessoas(), req.getLimitePessoas()));
        }
        if (req.getSprints() > req.getLimiteSprints()) {
            errors.put("sprints", String.format("Quantidade de sprints (%d) excede o limite permitido (%d)", req.getSprints(), req.getLimiteSprints()));
        }
        // prazo no request é fornecido em minutos; limiteTempo também é em minutos.
        Double prazoEmDias = null;
        if (req.getPrazo() != null) {
            prazoEmDias = req.getPrazo() / 480.0; // 480 minutos = 1 dia (8h)
            if (req.getPrazo() > req.getLimiteTempo()) {
                errors.put("prazo", String.format("Prazo informado (%d minutos / %.2f dias) excede o limite de tempo permitido (%d minutos)", req.getPrazo(), prazoEmDias, req.getLimiteTempo()));
            }
        }

        if (!errors.isEmpty()) {
            ErrorResponse err = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Limites excedidos", LocalDateTime.now(), errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        // cálculo principal usando DIAS_POR_SPRINT fixo
        double tempoEstimado = tempoEstimado(req.getPessoas(), req.getSprints());

        EstimativaResponseDTO resp = new EstimativaResponseDTO();
        resp.setTempoEstimado(tempoEstimado);

        // prazo opcional
        if (prazoEmDias != null) {
            double diferenca = prazoEmDias - tempoEstimado;
            resp.setDentroPrazo(diferenca >= 0);
            resp.setTempoRestante(Math.max(diferenca, 0));
        } else {
            resp.setDentroPrazo(true);
            resp.setTempoRestante(Math.max(tempoEstimado, 0));
        }

        // variações simples: +0, +5, +10 pessoas
        List<String> pessoasCenarios = new ArrayList<>();
        int basePessoas = req.getPessoas();
        for (int pessoas : new int[]{basePessoas, basePessoas + 5, basePessoas + 10}) {
            double t = tempoEstimado(pessoas, req.getSprints());
            pessoasCenarios.add(String.format("Pessoas: %d | Sprints: %d | Tempo estimado: %.2f dias", pessoas, req.getSprints(), t));
        }
        resp.setPessoasCenarios(pessoasCenarios);

        // variações simples de sprints: base, +4, limiteSprints
        List<String> sprintsCenarios = new ArrayList<>();
        int baseSprints = req.getSprints();
        int limiteSprints = req.getLimiteSprints();
        for (int sprints : new int[]{baseSprints, baseSprints + 4, limiteSprints}) {
            double t = tempoEstimado(req.getPessoas(), sprints);
            sprintsCenarios.add(String.format("Pessoas: %d | Sprints: %d | Tempo estimado: %.2f dias", req.getPessoas(), sprints, t));
        }
        resp.setSprintsCenarios(sprintsCenarios);

        // busca da melhor configuração dentro dos limites
        double melhorTempo = Double.MAX_VALUE;
        int melhoresPessoas = 1;
        int melhoresSprints = 1;
        // limiteTempo é recebido em minutos; converter para dias para comparar com tempoEstimado (dias)
        double limiteTempoEmDias = req.getLimiteTempo() / 480.0;
        for (int pessoas = 1; pessoas <= req.getLimitePessoas(); pessoas++) {
            for (int sprints = 1; sprints <= req.getLimiteSprints(); sprints++) {
                double t = tempoEstimado(pessoas, sprints);
                if (t <= limiteTempoEmDias && t < melhorTempo) {
                    melhorTempo = t;
                    melhoresPessoas = pessoas;
                    melhoresSprints = sprints;
                }
            }
        }

        // se não encontrou dentro dos limites, define valores nulos/indicativos
        if (melhorTempo == Double.MAX_VALUE) {
            resp.setMelhorPessoas(0);
            resp.setMelhorSprints(0);
            resp.setMelhorTempo(-1);
        } else {
            resp.setMelhorPessoas(melhoresPessoas);
            resp.setMelhorSprints(melhoresSprints);
            resp.setMelhorTempo(melhorTempo);
        }

        return ResponseEntity.ok(resp);
    }

    private double tempoEstimado(int qtdPessoas, int qtdSprints) {
        if (qtdPessoas <= 0 || qtdSprints <= 0) {
            throw new IllegalArgumentException("Pessoas e sprints devem ser maiores que zero.");
        }
        return (qtdSprints * DIAS_POR_SPRINT) / (double) qtdPessoas;
    }
}
