package com.fiap.sus.liveops.modules.attendance.dto;

import com.fiap.sus.liveops.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.liveops.modules.attendance.enums.RiskClassification;

import java.time.LocalDateTime;

public record AttendanceResponse(
        String id,
        String healthUnitId,
        String patientName,
        AttendanceStatus status,
        RiskClassification riskClassification,
        LocalDateTime entryTime
) {
}
