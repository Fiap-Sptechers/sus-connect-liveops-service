package com.fiap.sus.liveops.modules.attendance.controller;

import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.dto.AttendanceResponse;
import com.fiap.sus.liveops.modules.attendance.dto.CompleteAttendanceResponse;
import com.fiap.sus.liveops.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.liveops.modules.attendance.dto.TriageRequest;
import com.fiap.sus.liveops.modules.attendance.mapper.AttendanceMapper;
import com.fiap.sus.liveops.modules.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
@Tag(name = "Attendance", description = "Endpoints para gerenciamento de atendimentos nas unidades de saúde")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceMapper mapper;

    @Operation(
            summary = "Iniciar processo de atendimento de um paciente",
            description = """
                Inicia um novo atendimento para um paciente em uma unidade de saúde específica.
                O status inicial do atendimento será definido como 'WAITING'.
             """
    )
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

    @Operation(
            summary = "Listar atendimentos por unidade de saúde",
            description = """
                Retorna uma lista de atendimentos associados a uma unidade de saúde específica.
             """
    )
    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<AttendanceResponse>> listByUnitId(@PathVariable String unitId) {
        return ResponseEntity.ok(
                attendanceService.listByHealthUnitId(unitId).stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    @Operation(
            summary = "Obter detalhes de um atendimento",
            description = """
                Retorna os detalhes completos de um atendimento específico, incluindo informações do paciente, status atual e tempos de entrada e saída.
             """
    )
    @GetMapping("/{id}")
    public ResponseEntity<CompleteAttendanceResponse> getAttendanceById(
            @Schema(description = "ID do atendimento a ser recuperado", example = "698e65898661ef045bf53603")
            @PathVariable String id
    ) {
        Attendance attendance = attendanceService.getAttendanceById(id);
        CompleteAttendanceResponse response = mapper.toCompleteResponse(attendance);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Atualizar status de um atendimento",
            description = """
                Atualiza o status de um atendimento específico. Permite alterar o status para refletir o progresso do atendimento, como 'IN_PROGRESS' ou 'DISCHARGED'.
             """
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<CompleteAttendanceResponse> updateStatus(
            @Schema(description = "ID do atendimento a ser atualizado", example = "698e65898661ef045bf53603")
            @PathVariable String id,
            @RequestBody @Valid StatusUpdateRequest request
    ) {
        Attendance updatedAttendance = attendanceService.updateStatus(id, request);
        CompleteAttendanceResponse response = mapper.toCompleteResponse(updatedAttendance);
        return ResponseEntity.ok(response);
    }

}
