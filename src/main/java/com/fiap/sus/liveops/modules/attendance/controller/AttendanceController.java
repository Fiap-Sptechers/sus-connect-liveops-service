package com.fiap.sus.liveops.modules.attendance.controller;

import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.liveops.modules.attendance.dto.TriageRequest;
import com.fiap.sus.liveops.modules.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/triage")
    public ResponseEntity<Attendance> startTriage(
            @RequestBody @Valid TriageRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        Attendance attendance = attendanceService.startTriage(request);

        URI uri = uriBuilder.path("/attendances/{id}").buildAndExpand(attendance.getId()).toUri();

        return ResponseEntity.created(uri).body(attendance);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Attendance> updateStatus(
            @PathVariable String id,
            @RequestBody @Valid StatusUpdateRequest request
    ) {
        Attendance updatedAttendance = attendanceService.updateStatus(id, request);
        return ResponseEntity.ok(updatedAttendance);
    }

}
