package com.fiap.sus.liveops.modules.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UnitAnalytics(
        @Schema(description = "Identificador da unidade de saúde", example = "9ba937e6-0026-438c-829f-e050780cdda0")
        String healthUnitId,

        @Schema(description = "Média geral do tempo de atendimento em minutos", example = "45")
        long generalAverageWaitTimeMinutes,

        @Schema(description = "Dados atuais da fila de atendimento, incluindo número de pacientes")
        LiveQueueSnapshot queueSnapshot,

        @Schema(description = "Desempenho de atendimento por classificação de risco, incluindo tempos médio de atendimento e limites de SLA")
        List<RiskAttendancePerformance> riskPerformance
) {}
