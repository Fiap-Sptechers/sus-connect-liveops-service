package com.fiap.sus.liveops.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RiskClassification {

    RED(1, "Emergência",0),
    ORANGE(2, "Muito Urgente", 10),
    YELLOW(3, "Urgente", 60),
    GREEN(4, "Pouco Urgente", 120),
    BLUE(5, "Não Urgente", 240);

    private final int code;
    private final String description;
    private final int maxMinutesWaiting;

}
