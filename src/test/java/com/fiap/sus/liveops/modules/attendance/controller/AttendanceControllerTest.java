package com.fiap.sus.liveops.modules.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.liveops.core.exception.ResourceNotFoundException;
import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.dto.AttendanceResponse;
import com.fiap.sus.liveops.modules.attendance.dto.CompleteAttendanceResponse;
import com.fiap.sus.liveops.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.liveops.modules.attendance.dto.TriageRequest;
import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import com.fiap.sus.liveops.shared.enums.RiskClassification;
import com.fiap.sus.liveops.modules.attendance.mapper.AttendanceMapper;
import com.fiap.sus.liveops.modules.attendance.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AttendanceController.class, excludeAutoConfiguration = MongoAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"mongock.enabled=false"})
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService service;

    @MockitoBean
    private AttendanceMapper mapper;

    @Test
    void startTriage_ShouldReturnCreated() throws Exception {
        TriageRequest request = new TriageRequest(
                "US-01", "João", "123", RiskClassification.RED
        );

        Attendance attendance = new Attendance();
        attendance.setId("12345");

        AttendanceResponse response = new AttendanceResponse(
                "12345", "US-01", "João",
                AttendanceStatus.WAITING, RiskClassification.RED,
                LocalDateTime.now()
        );

        when(service.startTriage(any(TriageRequest.class))).thenReturn(attendance);
        when(mapper.toResponse(any(Attendance.class))).thenReturn(response);

        mockMvc.perform(post("/attendances/triage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value("12345"))
                .andExpect(jsonPath("$.patientName").value("João"));
    }

    @Test
    void startTriage_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Request vazio/inválido para testar o @Valid
        TriageRequest invalidRequest = new TriageRequest(
                "", "", "", null
        );

        mockMvc.perform(post("/attendances/triage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_ShouldReturnAttendance() throws Exception {
        String id = "12345";
        Attendance attendance = new Attendance();
        CompleteAttendanceResponse response = new CompleteAttendanceResponse(
                id, "US-01", "Maria", "12345678900",
                AttendanceStatus.WAITING, RiskClassification.RED,
                null, null, null
        );

        when(service.getAttendanceById(id)).thenReturn(attendance);
        when(mapper.toCompleteResponse(attendance)).thenReturn(response);

        mockMvc.perform(get("/attendances/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("Maria"));
    }

    @Test
    void getById_NotFound_ShouldReturn404() throws Exception {
        String id = "999";

        when(service.getAttendanceById(id))
                .thenThrow(new ResourceNotFoundException("Attendance not found"));

        mockMvc.perform(get("/attendances/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateStatus_ShouldReturnUpdatedAttendance() throws Exception {
        String id = "12345";
        StatusUpdateRequest request = new StatusUpdateRequest(AttendanceStatus.IN_PROGRESS);

        Attendance updatedAttendance = new Attendance();
        CompleteAttendanceResponse response = new CompleteAttendanceResponse(
                id, "US-01", "Carlos", "09876543211",
                AttendanceStatus.IN_PROGRESS, RiskClassification.RED,
                null, null, null
        );

        when(service.updateStatus(eq(id), any(StatusUpdateRequest.class)))
                .thenReturn(updatedAttendance);
        when(mapper.toCompleteResponse(updatedAttendance)).thenReturn(response);

        mockMvc.perform(patch("/attendances/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

}