package com.fiap.sus.liveops.modules.attendance.service;

import com.fiap.sus.liveops.core.exception.InvalidAttendanceStatusException;
import com.fiap.sus.liveops.core.exception.ResourceNotFoundException;
import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.document.embedded.Patient;
import com.fiap.sus.liveops.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.liveops.modules.attendance.dto.TriageRequest;
import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import com.fiap.sus.liveops.modules.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<Attendance> listByHealthUnitId(String healthUnitId) {
        return attendanceRepository.findByHealthUnitId(healthUnitId);
    }

    public Attendance getAttendanceById(String id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + id));
    }

    public Attendance updateStatus(String id, StatusUpdateRequest request) {
        log.info("Updating attendance {} to status {}", id, request.status());

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + id));

        validateStatusTransition(attendance.getStatus(), request.status());

        AttendanceStatus newStatus = request.status();
        attendance.setStatus(newStatus);

        if (newStatus == AttendanceStatus.IN_PROGRESS) {
            attendance.setStartTime(LocalDateTime.now());
        } else if (newStatus == AttendanceStatus.DISCHARGED) {
            attendance.setDischargeTime(LocalDateTime.now());
        }

        return attendanceRepository.save(attendance);
    }

    protected void validateStatusTransition(AttendanceStatus actualStatus, AttendanceStatus newStatus) {

        if (actualStatus == AttendanceStatus.DISCHARGED) {
            throw new InvalidAttendanceStatusException("Cannot change status of a discharged attendance. Start a new attendance.");
        }

        if (actualStatus == AttendanceStatus.WAITING && newStatus == AttendanceStatus.DISCHARGED) {
            throw new InvalidAttendanceStatusException("Invalid status transition from WAITING to DISCHARGED.");
        }

        if (actualStatus == AttendanceStatus.IN_PROGRESS && newStatus == AttendanceStatus.WAITING) {
            throw new InvalidAttendanceStatusException("Invalid status transition from IN_PROGRESS to WAITING.");
        }

    }

}
