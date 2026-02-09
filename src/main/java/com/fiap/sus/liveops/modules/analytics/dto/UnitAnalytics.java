package com.fiap.sus.liveops.modules.analytics.dto;

import java.util.List;

public record UnitAnalytics(
        String healthUnitId,
        long generalAverageWaitTimeMinutes,
        LiveQueueSnapshot queueSnapshot,
        List<RiskAttendancePerformance> riskPerformance
) {}
