package com.avantt_backend.controller;

import com.avantt_backend.dto.*;
import com.avantt_backend.service.MetricsService;
import com.avantt_backend.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.API + "/metricas")
@Tag(name = "Metricas", description = "Indicadores e métricas do projeto")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Operation(summary = "Distribuição de tarefas por status (pizza)")
    @GetMapping(value = "/project/{projectId}/status-distribution", produces = "application/json")
    public ResponseEntity<?> statusDistribution(@PathVariable("projectId") Integer projectId) {
        List<MetricStatusCountDTO> list = metricsService.getStatusDistribution(projectId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Taxa de conclusão e resumo")
    @GetMapping(value = "/project/{projectId}/overview", produces = "application/json")
    public ResponseEntity<?> overview(@PathVariable("projectId") Integer projectId) {
        MetricCompletionRateDTO rate = metricsService.getCompletionRate(projectId);
        Integer overdue = metricsService.getOverdueCount(projectId);
        Double hours = metricsService.getHoursEstimated(projectId);

        Map<String, Object> out = new HashMap<>();
        out.put("completion", rate == null ? new MetricCompletionRateDTO() : rate);
        out.put("overdueCount", overdue);
        out.put("hoursEstimated", hours);

        return ResponseEntity.ok(out);
    }

    @Operation(summary = "Top N tarefas mais atrasadas")
    @GetMapping(value = "/project/{projectId}/top-overdue", produces = "application/json")
    public ResponseEntity<?> topOverdue(@PathVariable("projectId") Integer projectId,
                                        @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        List<MetricOverdueTaskDTO> list = metricsService.getTopOverdue(projectId, limit);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Tarefas por membro")
    @GetMapping(value = "/project/{projectId}/tasks-by-member", produces = "application/json")
    public ResponseEntity<?> tasksByMember(@PathVariable("projectId") Integer projectId) {
        List<MetricTasksByMemberDTO> list = metricsService.getTasksByMember(projectId);
        return ResponseEntity.ok(list);
    }
}
