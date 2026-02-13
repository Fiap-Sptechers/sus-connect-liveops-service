package com.fiap.sus.liveops.modules.attendance.dto;

import com.fiap.sus.liveops.shared.enums.RiskClassification;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TriageRequest (
        @NotBlank(message = "Health Unit ID is required")
        @Schema(description = "ID da unidade de saúde onde o atendimento está sendo iniciado", example = "9ba937e6-0026-438c-829f-e050780cdda0")
        String healthUnitId,

        @Schema(description = "Nome do paciente", example = "João da Silva")
        @NotBlank(message = "Patient name is required")
        String patientName,

        @Schema(description = "CPF do paciente", example = "123.456.789-00")
        @NotBlank(message = "Patient document is required")
        String patientCpf,

        @Schema(description = "Classificação de risco do paciente", example = "BLUE")
        @NotNull(message = "Risk classification is required")
        RiskClassification riskClassification
) {}