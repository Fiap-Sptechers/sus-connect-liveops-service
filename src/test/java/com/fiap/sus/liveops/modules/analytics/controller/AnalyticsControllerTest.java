package com.fiap.sus.liveops.modules.analytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.liveops.modules.analytics.dto.LiveQueueSnapshot;
import com.fiap.sus.liveops.modules.analytics.dto.RiskAttendancePerformance;
import com.fiap.sus.liveops.modules.analytics.dto.UnitAnalytics;
import com.fiap.sus.liveops.modules.analytics.dto.UnitMetrics;
import com.fiap.sus.liveops.modules.analytics.service.AnalyticsService;
import com.fiap.sus.liveops.shared.enums.RiskClassification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AnalyticsController.class, excludeAutoConfiguration = MongoAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"mongock.enabled=false"})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    void getBasicUnitMetrics_ShouldReturnMetrics() throws Exception {
        String healthUnitId = "US-VILA-MARIANA";
        UnitMetrics metrics = new UnitMetrics(healthUnitId, 15L, 30L);

        when(analyticsService.getBasicMetrics(eq(healthUnitId))).thenReturn(metrics);

        mockMvc.perform(get("/analytics/units/{healthUnitId}", healthUnitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthUnitId").value(healthUnitId))
                .andExpect(jsonPath("$.queueSize").value(15))
                .andExpect(jsonPath("$.averageMinutes").value(30));
    }

    @Test
    void getBasicUnitMetrics_WithZeroValues_ShouldReturnMetrics() throws Exception {
        String healthUnitId = "US-EMPTY";
        UnitMetrics metrics = new UnitMetrics(healthUnitId, 0L, 0L);

        when(analyticsService.getBasicMetrics(eq(healthUnitId))).thenReturn(metrics);

        mockMvc.perform(get("/analytics/units/{healthUnitId}", healthUnitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthUnitId").value(healthUnitId))
                .andExpect(jsonPath("$.queueSize").value(0))
                .andExpect(jsonPath("$.averageMinutes").value(0));
    }

    @Test
    void getUnitAnalytics_ShouldReturnAdvancedAnalytics() throws Exception {
        String healthUnitId = "US-VILA-MARIANA";

        LiveQueueSnapshot snapshot = new LiveQueueSnapshot(20L, 12L, 8L);
        List<RiskAttendancePerformance> performances = Arrays.asList(
                new RiskAttendancePerformance(RiskClassification.RED, 15L, 10, true),
                new RiskAttendancePerformance(RiskClassification.YELLOW, 45L, 60, false)
        );
        UnitAnalytics analytics = new UnitAnalytics(healthUnitId, 35L, snapshot, performances);

        when(analyticsService.getAnalytics(eq(healthUnitId))).thenReturn(analytics);

        mockMvc.perform(get("/analytics/units/{healthUnitId}/advanced", healthUnitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthUnitId").value(healthUnitId))
                .andExpect(jsonPath("$.generalAverageWaitTimeMinutes").value(35))
                .andExpect(jsonPath("$.queueSnapshot.totalPatients").value(20))
                .andExpect(jsonPath("$.queueSnapshot.waitingCount").value(12))
                .andExpect(jsonPath("$.queueSnapshot.inProgressCount").value(8))
                .andExpect(jsonPath("$.riskPerformance[0].risk").value("RED"))
                .andExpect(jsonPath("$.riskPerformance[0].averageWaitTimeMinutes").value(15))
                .andExpect(jsonPath("$.riskPerformance[0].isSlaBreached").value(true))
                .andExpect(jsonPath("$.riskPerformance[1].risk").value("YELLOW"))
                .andExpect(jsonPath("$.riskPerformance[1].averageWaitTimeMinutes").value(45))
                .andExpect(jsonPath("$.riskPerformance[1].isSlaBreached").value(false));
    }

    @Test
    void getUnitAnalytics_WithEmptyQueue_ShouldReturnAnalytics() throws Exception {
        String healthUnitId = "US-EMPTY";

        LiveQueueSnapshot snapshot = new LiveQueueSnapshot(0L, 0L, 0L);
        UnitAnalytics analytics = new UnitAnalytics(healthUnitId, 0L, snapshot, Collections.emptyList());

        when(analyticsService.getAnalytics(eq(healthUnitId))).thenReturn(analytics);

        mockMvc.perform(get("/analytics/units/{healthUnitId}/advanced", healthUnitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthUnitId").value(healthUnitId))
                .andExpect(jsonPath("$.generalAverageWaitTimeMinutes").value(0))
                .andExpect(jsonPath("$.queueSnapshot.totalPatients").value(0))
                .andExpect(jsonPath("$.riskPerformance").isEmpty());
    }

    @Test
    void getAdvancedAnalyticsForUnits_ShouldReturnListOfAnalytics() throws Exception {
        List<String> healthUnitIds = Arrays.asList("US-VILA-MARIANA", "US-TATUAPE");

        LiveQueueSnapshot snapshot1 = new LiveQueueSnapshot(10L, 6L, 4L);
        LiveQueueSnapshot snapshot2 = new LiveQueueSnapshot(8L, 5L, 3L);

        List<UnitAnalytics> analyticsList = Arrays.asList(
                new UnitAnalytics("US-VILA-MARIANA", 25L, snapshot1, Collections.emptyList()),
                new UnitAnalytics("US-TATUAPE", 30L, snapshot2, Collections.emptyList())
        );

        when(analyticsService.getAnalyticsByUnits(anyList())).thenReturn(analyticsList);

        mockMvc.perform(post("/analytics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(healthUnitIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].healthUnitId").value("US-VILA-MARIANA"))
                .andExpect(jsonPath("$[0].generalAverageWaitTimeMinutes").value(25))
                .andExpect(jsonPath("$[1].healthUnitId").value("US-TATUAPE"))
                .andExpect(jsonPath("$[1].generalAverageWaitTimeMinutes").value(30));
    }

    @Test
    void getAdvancedAnalyticsForUnits_WithSingleUnit_ShouldReturnList() throws Exception {
        List<String> healthUnitIds = Collections.singletonList("US-VILA-MARIANA");

        LiveQueueSnapshot snapshot = new LiveQueueSnapshot(10L, 6L, 4L);
        List<UnitAnalytics> analyticsList = Collections.singletonList(
                new UnitAnalytics("US-VILA-MARIANA", 25L, snapshot, Collections.emptyList())
        );

        when(analyticsService.getAnalyticsByUnits(anyList())).thenReturn(analyticsList);

        mockMvc.perform(post("/analytics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(healthUnitIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].healthUnitId").value("US-VILA-MARIANA"));
    }

    @Test
    void getAdvancedAnalyticsForUnits_WithEmptyList_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/analytics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

}

