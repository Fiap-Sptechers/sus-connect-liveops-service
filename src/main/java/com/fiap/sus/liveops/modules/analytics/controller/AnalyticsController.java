package com.fiap.sus.liveops.modules.analytics.controller;

import com.fiap.sus.liveops.modules.analytics.dto.UnitAnalytics;
import com.fiap.sus.liveops.modules.analytics.dto.UnitMetrics;
import com.fiap.sus.liveops.modules.analytics.service.AnalyticsService;
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
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/units/{healthUnitId}")
    public ResponseEntity<UnitMetrics> getBasicUnitMetrics(@PathVariable String healthUnitId) {
        UnitMetrics metrics = analyticsService.getBasicMetrics(healthUnitId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/units/{healthUnitId}/advanced")
    public ResponseEntity<UnitAnalytics> getUnitAnalytics(@PathVariable String healthUnitId) {
        var analytics = analyticsService.getAnalytics(healthUnitId);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping()
    public ResponseEntity<List<UnitAnalytics>> getAdvancedAnalyticsForUnits(
            @RequestBody List<String> healthUnitIds
    ) {
        if (healthUnitIds == null || healthUnitIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<UnitAnalytics> analyticsList = analyticsService.getAnalyticsByUnits(healthUnitIds);
        return ResponseEntity.ok(analyticsList);
    }

}
