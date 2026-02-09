package com.fiap.sus.liveops.core.migrations;

import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.document.embedded.Patient;
import com.fiap.sus.liveops.modules.attendance.repository.AttendanceRepository;
import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import com.fiap.sus.liveops.shared.enums.RiskClassification;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@ChangeUnit(id = "001_initial_seed_data", order = "1", author = "luuh-oliveira")
public class DatabaseSeedMigration {

    private final AttendanceRepository repository;

    @Execution
    public void execute() {
        // --- CENÁRIO 1: UPA VILA MARIANA (Alta Demanda & Atrasos) ---
        String unitA = "US-VILA-MARIANA";

        // 1. Emergência esperando há 10 min (SLA BREACHED! Era pra ser 0+5 min)
        create(unitA, "Carlos Grave", RiskClassification.RED, AttendanceStatus.WAITING, 10, 0, 0);

        // 2. Pouco Urgente esperando há 3 horas (SLA BREACHED! Era pra ser 120 min)
        create(unitA, "Maria Demora", RiskClassification.GREEN, AttendanceStatus.WAITING, 180, 0, 0);

        // 3. Atendimento Normal (Em andamento)
        create(unitA, "João Checkup",  RiskClassification.BLUE, AttendanceStatus.IN_PROGRESS, 30, 5, 0);

        // 4. Histórico Recente (Atendido rápido agora há pouco -> Puxa a média pra baixo)
        create(unitA, "Ana Rápida", RiskClassification.GREEN, AttendanceStatus.DISCHARGED, 40, 30, 10);


        // --- CENÁRIO 2: UBS JARDIM PAULISTA (Vazia & Rápida) ---
        String unitB = "US-JARDIM-PAULISTA";

        // 1. Só uma pessoa sendo atendida agora
        create(unitB, "Pedro Solitário", RiskClassification.YELLOW, AttendanceStatus.IN_PROGRESS, 15, 5, 0);

        // 2. Vários atendimentos finalizados muito rápidos
        create(unitB, "Altair Alta", RiskClassification.BLUE, AttendanceStatus.DISCHARGED, 20, 10, 5);
        create(unitB, "Beatriz Boa",  RiskClassification.GREEN, AttendanceStatus.DISCHARGED, 25, 15, 5);


        // --- CENÁRIO 3: TESTE DA MÉDIA PONDERADA (Peso Temporal) ---
        String unitC = "US-TESTE-ALGORITMO";

        // Caso Antigo (Ontem): Demorou 5 HORAS (300 min).
        // Se fosse média simples, isso destruiria a reputação da unidade hoje.
        // Como é ponderada, deve ter pouco impacto.
        create(unitC, "Senhor Antigo", RiskClassification.YELLOW, AttendanceStatus.DISCHARGED, 1500, 1400, 1200);

        // Caso Novo (Agora): Demorou 15 min.
        // A média mostrada no app deve ser muito mais próxima de 15 do que de 300.
        create(unitC, "Jovem Agora", RiskClassification.YELLOW, AttendanceStatus.DISCHARGED, 20, 5, 0);
    }

    @RollbackExecution
    public void rollback() {
        repository.deleteAll();
    }

    private void create(String unitId, String name, RiskClassification risk,
                        AttendanceStatus status,
                        long minutesAgoEntry, long minutesAgoStart, long minutesAgoDischarge
    ) {

        Patient patient = new Patient(null, name);
        Attendance att = new Attendance(unitId, risk, patient);
        att.setStatus(status);

        LocalDateTime now = LocalDateTime.now();

        att.setEntryTime(now.minusMinutes(minutesAgoEntry));

        if (status == AttendanceStatus.IN_PROGRESS || status == AttendanceStatus.DISCHARGED) {
            att.setStartTime(now.minusMinutes(minutesAgoStart));
        }

        if (status == AttendanceStatus.DISCHARGED) {
            att.setDischargeTime(now.minusMinutes(minutesAgoDischarge));
        }

        repository.save(att);
    }

}
