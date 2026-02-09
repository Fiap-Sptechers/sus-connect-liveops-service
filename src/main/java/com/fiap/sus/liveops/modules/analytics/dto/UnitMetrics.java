package com.fiap.sus.liveops.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnitMetrics {

    private String healthUnitId;
    private long queueSize;
    private long averageMinutes;

}
