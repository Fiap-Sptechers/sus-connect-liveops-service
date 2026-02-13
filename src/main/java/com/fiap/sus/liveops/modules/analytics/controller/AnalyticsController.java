package com.fiap.sus.liveops.modules.analytics.controller;

import com.fiap.sus.liveops.modules.analytics.dto.UnitAnalytics;
import com.fiap.sus.liveops.modules.analytics.dto.UnitMetrics;
import com.fiap.sus.liveops.modules.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Endpoints para análise de métricas e dados das unidades de saúde")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Deprecated
    @GetMapping("/units/{healthUnitId}")
    public ResponseEntity<UnitMetrics> getBasicUnitMetrics(@PathVariable String healthUnitId) {
        UnitMetrics metrics = analyticsService.getBasicMetrics(healthUnitId);
        return ResponseEntity.ok(metrics);
    }

    @Operation(
            summary = "Obter análises avançadas para uma unidade de saúde",
            description = "Retorna métricas detalhadas e insights para a unidade de saúde especificada, incluindo tempo médio de atendimento, taxa de ocupação, e outras análises relevantes."
    )
    @GetMapping("/units/{healthUnitId}/advanced")
    public ResponseEntity<UnitAnalytics> getUnitAnalytics(@PathVariable String healthUnitId) {
        var analytics = analyticsService.getAnalytics(healthUnitId);
        return ResponseEntity.ok(analytics);
    }

    @Operation(
            summary = "Obter análises avançadas para múltiplas unidades de saúde",
            description = "Permite obter métricas detalhadas e insights para uma lista de unidades de saúde, facilitando a comparação e análise entre elas."
    )
    @PostMapping()
    public ResponseEntity<List<UnitAnalytics>> getAdvancedAnalyticsForUnits(
            @Schema(description = "Lista de identificadores das unidades de saúde para as quais as análises devem ser obtidas", example = "[\"9ba937e6-0026-438c-829f-e050780cdda0\"]")
            @RequestBody List<String> healthUnitIds
    ) {
        if (healthUnitIds == null || healthUnitIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<UnitAnalytics> analyticsList = analyticsService.getAnalyticsByUnits(healthUnitIds);
        return ResponseEntity.ok(analyticsList);
    }

}
