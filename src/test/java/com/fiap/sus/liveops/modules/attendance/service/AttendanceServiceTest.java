package com.fiap.sus.liveops.modules.attendance.service;

import com.fiap.sus.liveops.core.exception.InvalidAttendanceStatusException;
import com.fiap.sus.liveops.core.exception.ResourceNotFoundException;
import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.liveops.modules.attendance.dto.TriageRequest;
import com.fiap.sus.liveops.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.liveops.modules.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Nested
    class StartTriage {

        @Test
        void shouldSaveAndReturnSavedAttendance_whenRequestIsValid() {
            TriageRequest request = mock(TriageRequest.class);
            when(request.patientName()).thenReturn("João Silva");
            when(request.patientCpf()).thenReturn("12345678900");
            when(request.healthUnitId()).thenReturn("unit-1");
            when(request.riskClassification()).thenReturn(null);

            Attendance saved = mock(Attendance.class);
            when(attendanceRepository.save(any(Attendance.class))).thenReturn(saved);

            Attendance result = attendanceService.startTriage(request);

            assertSame(saved, result);
            verify(attendanceRepository, times(1)).save(any(Attendance.class));
        }

        @Test
        void shouldPopulateAttendanceFieldsFromRequest_whenRequestIsValid() throws Exception {
            TriageRequest request = mock(TriageRequest.class);
            when(request.patientName()).thenReturn("Mariana Silva");
            when(request.patientCpf()).thenReturn("98765432100");
            when(request.healthUnitId()).thenReturn("unit-2");
            when(request.riskClassification()).thenReturn(null);

            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            attendanceService.startTriage(request);

            ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceRepository).save(captor.capture());
            Attendance captured = captor.getValue();

            Field healthUnitField = Attendance.class.getDeclaredField("healthUnitId");
            healthUnitField.setAccessible(true);
            Object healthUnitId = healthUnitField.get(captured);
            assertEquals("unit-2", healthUnitId);

            Field patientField = Attendance.class.getDeclaredField("patient");
            patientField.setAccessible(true);
            Object patient = patientField.get(captured);
            assertNotNull(patient);

            Field cpfField = patient.getClass().getDeclaredField("cpf");
            cpfField.setAccessible(true);
            Object cpf = cpfField.get(patient);
            assertEquals("98765432100", cpf);

            Field nameField = patient.getClass().getDeclaredField("name");
            nameField.setAccessible(true);
            Object name = nameField.get(patient);
            assertEquals("Mariana Silva", name);
        }

    }

    @Nested
    class GetAttendanceById {

        @Test
        void shouldReturnAttendance_whenAttendanceExists() {
            String id = "6985297d21f1ddcdaae711c0";
            Attendance attendance = mock(Attendance.class);
            when(attendanceRepository.findById(id)).thenReturn(java.util.Optional.of(attendance));

            Attendance result = attendanceService.getAttendanceById(id);

            assertSame(attendance, result);
            verify(attendanceRepository, times(1)).findById(id);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenAttendanceDoesNotExist() {
            when(attendanceRepository.findById("missing")).thenReturn(java.util.Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> attendanceService.getAttendanceById("missing"));
            verify(attendanceRepository, times(1)).findById("missing");
        }

    }

    @Nested
    class UpdateStatus {

        @Test
        void shouldUpdateStatusToInProgressAndSetStartTime_whenTransitionFromWaitingToInProgress() {
            String id = "att-123";
            Attendance attendance = mock(Attendance.class);
            when(attendance.getStatus()).thenReturn(AttendanceStatus.WAITING);

            StatusUpdateRequest request = mock(StatusUpdateRequest.class);
            when(request.newStatus()).thenReturn(AttendanceStatus.IN_PROGRESS);

            when(attendanceRepository.findById(id)).thenReturn(java.util.Optional.of(attendance));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Attendance result = attendanceService.updateStatus(id, request);

            verify(attendance).setStatus(AttendanceStatus.IN_PROGRESS);
            verify(attendance).setStartTime(any(java.time.LocalDateTime.class));
            verify(attendanceRepository, times(1)).save(attendance);
            assertSame(attendance, result);
        }

        @Test
        void shouldUpdateStatusToDischargedAndSetDischargeTime_whenTransitionFromInProgressToDischarged() {
            String id = "att-456";
            Attendance attendance = mock(Attendance.class);
            when(attendance.getStatus()).thenReturn(AttendanceStatus.IN_PROGRESS);

            StatusUpdateRequest request = mock(StatusUpdateRequest.class);
            when(request.newStatus()).thenReturn(AttendanceStatus.DISCHARGED);

            when(attendanceRepository.findById(id)).thenReturn(java.util.Optional.of(attendance));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Attendance result = attendanceService.updateStatus(id, request);

            verify(attendance).setStatus(AttendanceStatus.DISCHARGED);
            verify(attendance).setDischargeTime(any(java.time.LocalDateTime.class));
            verify(attendanceRepository, times(1)).save(attendance);
            assertSame(attendance, result);
        }

        @Test
        void shouldAllowNoOpWhenNewStatusIsSameAndNotSetTimes() {
            String id = "att-789";
            Attendance attendance = mock(Attendance.class);
            when(attendance.getStatus()).thenReturn(AttendanceStatus.WAITING);

            StatusUpdateRequest request = mock(StatusUpdateRequest.class);
            when(request.newStatus()).thenReturn(AttendanceStatus.WAITING);

            when(attendanceRepository.findById(id)).thenReturn(java.util.Optional.of(attendance));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Attendance result = attendanceService.updateStatus(id, request);

            verify(attendance).setStatus(AttendanceStatus.WAITING);
            verify(attendance, never()).setStartTime(any(java.time.LocalDateTime.class));
            verify(attendance, never()).setDischargeTime(any(java.time.LocalDateTime.class));
            verify(attendanceRepository, times(1)).save(attendance);
            assertSame(attendance, result);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenAttendanceDoesNotExist() {
            String id = "missing-id";
            StatusUpdateRequest request = mock(StatusUpdateRequest.class);
            when(request.newStatus()).thenReturn(AttendanceStatus.IN_PROGRESS);

            when(attendanceRepository.findById(id)).thenReturn(java.util.Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> attendanceService.updateStatus(id, request));
            verify(attendanceRepository, never()).save(any(Attendance.class));
        }

        @Test
        void shouldThrowInvalidAttendanceStatusException_whenInvalidTransitionAttempted() {
            String id = "att-000";
            Attendance attendance = mock(Attendance.class);
            when(attendance.getStatus()).thenReturn(AttendanceStatus.WAITING);

            StatusUpdateRequest request = mock(StatusUpdateRequest.class);
            when(request.newStatus()).thenReturn(AttendanceStatus.DISCHARGED);

            when(attendanceRepository.findById(id)).thenReturn(java.util.Optional.of(attendance));

            assertThrows(InvalidAttendanceStatusException.class, () -> attendanceService.updateStatus(id, request));
            verify(attendance, never()).setStatus(any(AttendanceStatus.class));
            verify(attendanceRepository, never()).save(any(Attendance.class));
        }

    }

    @Nested
    class ValidateStatusTransition {

        @ParameterizedTest
        @CsvSource({
                "WAITING,IN_PROGRESS",
                "IN_PROGRESS,DISCHARGED",
                "WAITING,WAITING",
                "IN_PROGRESS,IN_PROGRESS"
        })
        void shouldAllowValidTransitions(AttendanceStatus actual, AttendanceStatus newStatus) {
            assertDoesNotThrow(() -> attendanceService.validateStatusTransition(actual, newStatus));
        }

        @ParameterizedTest
        @CsvSource({
                "DISCHARGED,WAITING",
                "DISCHARGED,IN_PROGRESS",
                "DISCHARGED,DISCHARGED",
                "WAITING,DISCHARGED",
                "IN_PROGRESS,WAITING"
        })
        void shouldNotAllowInvalidTransitions(AttendanceStatus actual, AttendanceStatus newStatus) {
            assertThrows(InvalidAttendanceStatusException.class,
                    () -> attendanceService.validateStatusTransition(actual, newStatus));
        }

    }

}