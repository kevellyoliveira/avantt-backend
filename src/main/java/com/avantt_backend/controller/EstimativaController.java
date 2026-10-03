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

@RestController
@RequestMapping(ApiPaths.ESTIMATIVAS)
public class EstimativaController {

    private static final int DEFAULT_DIAS_POR_SPRINT = 15;

    @PostMapping(value = "/calc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> calcular(@Valid @RequestBody EstimativaRequestDTO req) {

        Map<String, String> errors = new HashMap<>();
        if (req.getPessoas() > req.getLimitePessoas()) {
            errors.put("pessoas", String.format("Quantidade de pessoas (%d) excede o limite permitido (%d)", req.getPessoas(), req.getLimitePessoas()));
        }
        if (req.getSprints() > req.getLimiteSprints()) {
            errors.put("sprints", String.format("Quantidade de sprints (%d) excede o limite permitido (%d)", req.getSprints(), req.getLimiteSprints()));
        }

        Double prazoEmDias = null;
        if (req.getPrazo() != null) {
            prazoEmDias = req.getPrazo() / 480.0; // 480 minutos = 1 dia (8h)
            if (req.getPrazo() > req.getLimiteTempo()) {
                errors.put("prazo", String.format("Prazo informado (%d minutos / %.2f dias) excede o limite de tempo permitido (%d minutos)", req.getPrazo(), prazoEmDias, req.getLimiteTempo()));
            }
        }


        if (req.getQuantidadeDiasSprint() != null && req.getQuantidadeDiasSprint() < DEFAULT_DIAS_POR_SPRINT) {
            errors.put("quantidadeDiasSprint", String.format("Quantidade mínima de dias por sprint é %d", DEFAULT_DIAS_POR_SPRINT));
        }

        if (!errors.isEmpty()) {
            ErrorResponse err = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Limites excedidos", LocalDateTime.now(), errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        int diasPorSprint = (req.getQuantidadeDiasSprint() == null) ? DEFAULT_DIAS_POR_SPRINT : req.getQuantidadeDiasSprint();
        double tempoEstimado = tempoEstimado(req.getPessoas(), req.getSprints(), diasPorSprint);

        EstimativaResponseDTO resp = new EstimativaResponseDTO();
        resp.setTempoEstimado(tempoEstimado);


        if (prazoEmDias != null) {
            double diferenca = prazoEmDias - tempoEstimado;
            resp.setDentroPrazo(diferenca >= 0);
            resp.setTempoRestante(Math.max(diferenca, 0));
        } else {
            resp.setDentroPrazo(true);
            resp.setTempoRestante(Math.max(tempoEstimado, 0));
        }


        List<String> pessoasCenarios = new ArrayList<>();
        int basePessoas = req.getPessoas();
        for (int pessoas : new int[]{basePessoas, basePessoas + 5, basePessoas + 10}) {
            double t = tempoEstimado(pessoas, req.getSprints(), diasPorSprint);
            pessoasCenarios.add(String.format("Pessoas: %d | Sprints: %d | Tempo estimado: %.2f dias", pessoas, req.getSprints(), t));
        }
        resp.setPessoasCenarios(pessoasCenarios);


        List<String> sprintsCenarios = new ArrayList<>();
        int baseSprints = req.getSprints();
        int limiteSprints = req.getLimiteSprints();
        for (int sprints : new int[]{baseSprints, baseSprints + 4, limiteSprints}) {
            double t = tempoEstimado(req.getPessoas(), sprints, diasPorSprint);
            sprintsCenarios.add(String.format("Pessoas: %d | Sprints: %d | Tempo estimado: %.2f dias", req.getPessoas(), sprints, t));
        }
        resp.setSprintsCenarios(sprintsCenarios);


        double melhorTempo = Double.MAX_VALUE;
        int melhoresPessoas = 1;
        int melhoresSprints = 1;

        double limiteTempoEmDias = req.getLimiteTempo() / 480.0;
        for (int pessoas = 1; pessoas <= req.getLimitePessoas(); pessoas++) {
            for (int sprints = 1; sprints <= req.getLimiteSprints(); sprints++) {
                double t = tempoEstimado(pessoas, sprints, diasPorSprint);
            if (t <= limiteTempoEmDias && t < melhorTempo) {
                    melhorTempo = t;
                    melhoresPessoas = pessoas;
                    melhoresSprints = sprints;
                }
            }
        }


        if (melhorTempo == Double.MAX_VALUE) {
            resp.setMelhorPessoas(0);
            resp.setMelhorSprints(0);
            resp.setMelhorTempo(-1);
            resp.setMelhorMensagem("Nenhuma combinação de pessoas/sprints atende ao limite de tempo informado.");
        } else {
            resp.setMelhorPessoas(melhoresPessoas);
            resp.setMelhorSprints(melhoresSprints);
            resp.setMelhorTempo(melhorTempo);
            resp.setMelhorMensagem(String.format("Melhor combinação: %d pessoa(s), %d sprint(s). Tempo estimado: %.2f dias. Observação: 'melhorTempo' está em dias e representa o menor tempo que não ultrapassa o limite de tempo informado.", melhoresPessoas, melhoresSprints, melhorTempo));
        }

        return ResponseEntity.ok(resp);
    }

    private double tempoEstimado(int qtdPessoas, int qtdSprints, int diasPorSprint) {
        if (qtdPessoas <= 0 || qtdSprints <= 0) {
            throw new IllegalArgumentException("Pessoas e sprints devem ser maiores que zero.");
        }
        if (diasPorSprint < DEFAULT_DIAS_POR_SPRINT) {
            throw new IllegalArgumentException("Quantidade mínima de dias por sprint é " + DEFAULT_DIAS_POR_SPRINT);
        }
        return (qtdSprints * diasPorSprint) / (double) qtdPessoas;
    }
}
