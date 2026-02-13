package com.fiap.sus.liveops.modules.attendance.dto;

import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull
        @Schema(description = "Novo status do atendimento", example = "IN_PROGRESS")
        AttendanceStatus newStatus
) {}
