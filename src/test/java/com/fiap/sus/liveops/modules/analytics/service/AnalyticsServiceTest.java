package com.fiap.sus.liveops.modules.analytics.service;

import com.fiap.sus.liveops.modules.analytics.dto.UnitMetrics;
import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import com.fiap.sus.liveops.modules.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AttendanceRepository repository;

    @InjectMocks
    private AnalyticsService service;

    @Nested
    class GetMetrics {

        @Test
        @DisplayName("Should return zero metrics when there are no attendances for the health unit")
        void shouldReturnZeroMetricsWhenNoAttendances() {
            when(repository.findByHealthUnitIdAndStatusNot("unit-1", AttendanceStatus.DISCHARGED))
                    .thenReturn(Collections.emptyList());
            when(repository.findByHealthUnitIdAndStatus("unit-1", AttendanceStatus.DISCHARGED))
                    .thenReturn(Collections.emptyList());

             UnitMetrics metrics = service.getBasicMetrics("unit-1");

            assertEquals("unit-1", metrics.getHealthUnitId());
            assertEquals(0L, metrics.getQueueSize());
            assertEquals(0L, metrics.getAverageMinutes());
        }

        @Test
        @DisplayName("Should calculate queue size excluding discharged attendances")
        void shouldCalculateQueueSizeExcludingDischargedAttendances() {

            Attendance a1 = mock(Attendance.class);
            Attendance a2 = mock(Attendance.class);
            Attendance a3 = mock(Attendance.class);

            when(repository.findByHealthUnitIdAndStatus("unit-2", AttendanceStatus.DISCHARGED))
                    .thenReturn(Collections.emptyList());
            when(repository.findByHealthUnitIdAndStatusNot("unit-2", AttendanceStatus.DISCHARGED))
                    .thenReturn(Arrays.asList(a1, a2, a3));

            UnitMetrics metrics = service.getBasicMetrics("unit-2");

            assertEquals("unit-2", metrics.getHealthUnitId());
            assertEquals(3L, metrics.getQueueSize());
        }

        @Test
        @DisplayName("Should compute average minutes ignoring incomplete records and truncate to long")
        void shouldComputeAverageMinutesCorrectly() {
            LocalDateTime now = LocalDateTime.now();

            Attendance a1 = mock(Attendance.class);
            Attendance a2 = mock(Attendance.class);

            when(a1.getEntryTime()).thenReturn(now.minusMinutes(30));
            when(a1.getDischargeTime()).thenReturn(now);

            when(a2.getEntryTime()).thenReturn(now.minusMinutes(45));
            when(a2.getDischargeTime()).thenReturn(now);

            when(repository.findByHealthUnitIdAndStatusNot("unit-3", AttendanceStatus.DISCHARGED))
                    .thenReturn(Collections.emptyList());
            when(repository.findByHealthUnitIdAndStatus("unit-3", AttendanceStatus.DISCHARGED))
                    .thenReturn(Arrays.asList(a1, a2));

            UnitMetrics metrics = service.getBasicMetrics("unit-3");

            assertEquals("unit-3", metrics.getHealthUnitId());
            assertEquals(0L, metrics.getQueueSize());
            assertEquals(37L, metrics.getAverageMinutes());
        }

        @Test
        @DisplayName("Should return zero average when finished attendances have no complete timestamps")
        void shouldReturnZeroAverageWhenFinishedAttendancesHaveNoCompleteTimestamps() {
            Attendance f1 = mock(Attendance.class);
            Attendance f2 = mock(Attendance.class);

            when(f1.getEntryTime()).thenReturn(null);

            when(f2.getEntryTime()).thenReturn(LocalDateTime.now());
            when(f2.getDischargeTime()).thenReturn(null);

            when(repository.findByHealthUnitIdAndStatusNot("unit-4", AttendanceStatus.DISCHARGED))
                    .thenReturn(Collections.emptyList());
            when(repository.findByHealthUnitIdAndStatus("unit-4", AttendanceStatus.DISCHARGED))
                    .thenReturn(Arrays.asList(f1, f2));

            UnitMetrics metrics = service.getBasicMetrics("unit-4");

            assertEquals("unit-4", metrics.getHealthUnitId());
            assertEquals(0L, metrics.getQueueSize());
            assertEquals(0L, metrics.getAverageMinutes());
        }

    }

}