package com.fiap.sus.liveops.modules.attendance.service;

import com.fiap.sus.liveops.core.exception.ResourceNotFoundException;
import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.document.embedded.Patient;
import com.fiap.sus.liveops.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.liveops.modules.attendance.dto.TriageRequest;
import com.fiap.sus.liveops.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.liveops.modules.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public Attendance updateStatus(String id, StatusUpdateRequest request) {
        log.info("Updating attendance {} to status {}", id, request.newStatus());

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + id));

        AttendanceStatus newStatus = request.newStatus();
        attendance.setStatus(newStatus);

        if (newStatus == AttendanceStatus.IN_PROGRESS) {
            attendance.setStartTime(LocalDateTime.now());
        } else if (newStatus == AttendanceStatus.DISCHARGED) {
            attendance.setDischargeTime(LocalDateTime.now());
        }

        return attendanceRepository.save(attendance);
    }

}
