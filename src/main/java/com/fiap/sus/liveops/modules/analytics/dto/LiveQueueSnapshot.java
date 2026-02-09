package com.fiap.sus.liveops.modules.analytics.dto;

public record LiveQueueSnapshot(
        long totalPatients,
        long waitingCount,
        long inProgressCount
) {
}
