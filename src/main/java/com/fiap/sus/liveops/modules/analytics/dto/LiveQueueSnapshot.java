package com.fiap.sus.liveops.modules.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LiveQueueSnapshot(
        @Schema(description = "Número total de pacientes atualmente na fila, incluindo os que estão aguardando atendimento e os que estão em atendimento", example = "25")
        long totalPatients,

        @Schema(description = "Número de pacientes que estão aguardando atendimento", example = "15")
        long waitingCount,

        @Schema(description = "Número de pacientes que estão atualmente em atendimento", example = "10")
        long inProgressCount
) {
}
