package com.fiap.sus.liveops.modules.attendance.service;

import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.document.embedded.Patient;
import com.fiap.sus.liveops.modules.attendance.dto.TriageRequest;
import com.fiap.sus.liveops.modules.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public Attendance startTriage(TriageRequest request) {
        log.info("Starting triage for patient {} at unit {}", request.patientName(), request.healthUnitId());

        Patient patient = new Patient(request.patientCpf(), request.patientName());

        Attendance attendance = new Attendance(
                request.healthUnitId(),
                request.riskClassification(),
                patient
        );

        return attendanceRepository.save(attendance);
    }

}
