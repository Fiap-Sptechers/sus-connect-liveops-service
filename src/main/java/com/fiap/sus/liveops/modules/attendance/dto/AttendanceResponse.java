package com.fiap.sus.liveops.modules.attendance.dto;

import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import com.fiap.sus.liveops.shared.enums.RiskClassification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AttendanceResponse(
        @Schema(description = "ID do atendimento", example = "698e65898661ef045bf53603")
        String id,

        @Schema(description = "ID da unidade de saúde onde o atendimento está ocorrendo", example = "9ba937e6-0026-438c-829f-e050780cdda0")
        String healthUnitId,

        @Schema(description = "Nome do paciente", example = "João da Silva")
        String patientName,

        @Schema(description = "Status atual do atendimento", example = "WAITING")
        AttendanceStatus status,

        @Schema(description = "Classificação de risco do paciente", example = "BLUE")
        RiskClassification riskClassification,

        @Schema(description = "Data e hora de início do processo de atendimento", example = "2026-02-12T20:03:05.458")
        LocalDateTime entryTime
) {
}
