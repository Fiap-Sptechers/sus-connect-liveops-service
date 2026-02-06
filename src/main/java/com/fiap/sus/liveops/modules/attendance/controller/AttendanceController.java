package com.fiap.sus.liveops.modules.attendance.controller;

import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.dto.AttendanceResponse;
import com.fiap.sus.liveops.modules.attendance.dto.CompleteAttendanceResponse;
import com.fiap.sus.liveops.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.liveops.modules.attendance.dto.TriageRequest;
import com.fiap.sus.liveops.modules.attendance.mapper.AttendanceMapper;
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
    private final AttendanceMapper mapper;

    @PostMapping("/triage")
    public ResponseEntity<AttendanceResponse> startTriage(
            @RequestBody @Valid TriageRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        Attendance attendance = attendanceService.startTriage(request);
        AttendanceResponse response = mapper.toResponse(attendance);

        URI uri = uriBuilder.path("/attendances/{id}").buildAndExpand(attendance.getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompleteAttendanceResponse> getAttendanceById(@PathVariable String id) {
        Attendance attendance = attendanceService.getAttendanceById(id);
        CompleteAttendanceResponse response = mapper.toCompleteResponse(attendance);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CompleteAttendanceResponse> updateStatus(
            @PathVariable String id,
            @RequestBody @Valid StatusUpdateRequest request
    ) {
        Attendance updatedAttendance = attendanceService.updateStatus(id, request);
        CompleteAttendanceResponse response = mapper.toCompleteResponse(updatedAttendance);
        return ResponseEntity.ok(response);
    }

}
