package com.fiap.sus.liveops.modules.analytics.dto;

import com.fiap.sus.liveops.shared.enums.RiskClassification;
import io.swagger.v3.oas.annotations.media.Schema;

public record RiskAttendancePerformance(
        @Schema(description = "Classificação de risco associada a este desempenho de atendimento", example = "BLUE")
        RiskClassification risk,

        @Schema(description = "Tempo médio de espera para pacientes desta classificação de risco, em minutos", example = "60")
        long averageWaitTimeMinutes,

        @Schema(description = "Limite de tempo em minutos para atendimento dentro do SLA para esta classificação de risco", example = "240")
        int maxWaitTimeLimit,

        @Schema(description = "Indica se existem atendimentos para esta classificação de risco que ultrapassaram o limite de SLA definido", example = "false")
        boolean isSlaBreached
) {
}
