package com.fiap.sus.liveops.modules.attendance.document;

import com.fiap.sus.liveops.modules.attendance.document.embedded.Patient;
import com.fiap.sus.liveops.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.liveops.modules.attendance.enums.RiskClassification;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "attendances")
public class Attendance {

    @Id
    private String id;

    private String healthUnitId;

    private Patient patient;

    private AttendanceStatus status;

    private RiskClassification riskClassification;

    private LocalDateTime entryTime;

    private LocalDateTime startTime;

    private LocalDateTime dischargeTime;

    public Attendance(String healthUnitId, RiskClassification riskClassification, Patient patient) {
        this.healthUnitId = healthUnitId;
        this.riskClassification = riskClassification;
        this.patient = patient;
        this.status = AttendanceStatus.WAITING;
        this.entryTime = LocalDateTime.now();
    }

}
