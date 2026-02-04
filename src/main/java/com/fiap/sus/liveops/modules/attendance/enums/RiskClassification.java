package com.fiap.sus.liveops.modules.attendance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RiskClassification {

    RED(1, "Emergência"),
    ORANGE(2, "Muito Urgente"),
    YELLOW(3, "Urgente"),
    GREEN(4, "Pouco Urgente"),
    BLUE(5, "Não Urgente");

    private final int code;
    private final String description;

}
