package com.fiap.sus.liveops.modules.attendance.dto;

import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull
        AttendanceStatus status
) {}
