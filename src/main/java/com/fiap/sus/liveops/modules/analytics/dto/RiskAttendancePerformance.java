package com.fiap.sus.liveops.modules.analytics.dto;

import com.fiap.sus.liveops.shared.enums.RiskClassification;

public record RiskAttendancePerformance(
        RiskClassification risk,
        long averageWaitTimeMinutes,
        int maxWaitTimeLimit,
        boolean isSlaBreached
) {
}
