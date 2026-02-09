package com.fiap.sus.liveops.modules.analytics.service;

import com.fiap.sus.liveops.modules.analytics.dto.*;
import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.repository.AttendanceRepository;
import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import com.fiap.sus.liveops.shared.enums.RiskClassification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AttendanceRepository repository;
    private final Executor taskExecutor;

    private static final int ANALYSIS_WINDOW_HOURS = 12;

    public UnitMetrics getBasicMetrics(String healthUnitId) {
        log.info("Calculating metrics for unit: {}", healthUnitId);

        // Calculate queue size from attendances that are not discharged
        List<Attendance> activeQueue = repository.findByHealthUnitIdAndStatusNot(
                healthUnitId,
                AttendanceStatus.DISCHARGED
        );
        long queueSize = activeQueue.size();

        // Calculate average time in minutes from entry to discharge for discharged attendances
        List<Attendance> finishedAttendances = repository.findByHealthUnitIdAndStatus(
                healthUnitId,
                AttendanceStatus.DISCHARGED
        );

        double averageMinutes = finishedAttendances.stream()
                .filter(a -> a.getEntryTime() != null && a.getDischargeTime() != null)
                .mapToLong(a -> Duration.between(a.getEntryTime(), a.getDischargeTime()).toMinutes())
                .average()
                .orElse(0.0);

        return new UnitMetrics(healthUnitId, queueSize, (long) averageMinutes);
    }

    public List<UnitAnalytics> getAnalyticsByUnits(List<String> healthUnitIds) {
        log.info("Starting batch analytics for {} units", healthUnitIds.size());

        List<CompletableFuture<UnitAnalytics>> futures = healthUnitIds.stream()
                .map(id -> CompletableFuture.supplyAsync(
                        () -> getAnalytics(id),
                        taskExecutor
                ))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public UnitAnalytics getAnalytics(String healthUnitId) {
        log.info("Generating advanced analytics for unit: {}", healthUnitId);

        LocalDateTime cutoff = LocalDateTime.now().minusHours(ANALYSIS_WINDOW_HOURS);
        List<Attendance> attendances = repository.findByHealthUnitIdAndEntryTimeAfter(healthUnitId, cutoff);

        // Step 1: Get live queue snapshot
        LiveQueueSnapshot snapshot = getLiveQueueSnapshot(attendances);

        // Step 2: Calculate general weighted wait time
        long generalWeightedWait = getGeneralWeightedWait(attendances);

        // Step 3: Calculate risk-based performance
        List<RiskAttendancePerformance> performances = getRiskAttendancePerformances(attendances);

        return new UnitAnalytics(healthUnitId, generalWeightedWait, snapshot, performances);
    }

    protected long calculateEffectiveWaitMinutes(Attendance a) {
        LocalDateTime end = (a.getStartTime() != null) ? a.getStartTime() : LocalDateTime.now();
        return Duration.between(a.getEntryTime(), end).toMinutes();
    }

    protected long getGeneralWeightedWait(List<Attendance> attendances) {
        List<Attendance> servedPatients = attendances.stream()
                .filter(a -> a.getStartTime() != null)
                .toList();

        return calculateWeightedWaitTime(servedPatients);
    }

    protected static LiveQueueSnapshot getLiveQueueSnapshot(List<Attendance> allAttendances) {
        List<Attendance> activePatients = allAttendances.stream()
                .filter(a -> a.getStatus() != AttendanceStatus.DISCHARGED)
                .toList();

        long waiting = activePatients.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.WAITING).count();

        long inProgress = activePatients.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.IN_PROGRESS).count();

        return new LiveQueueSnapshot(
                activePatients.size(),
                waiting,
                inProgress
        );
    }

    protected List<RiskAttendancePerformance> getRiskAttendancePerformances(List<Attendance> attendances) {
        Map<RiskClassification, List<Attendance>> byRisk = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getRiskClassification));

        List<RiskAttendancePerformance> performances = new ArrayList<>();

        for (RiskClassification risk : RiskClassification.values()) {
            List<Attendance> riskPatients = byRisk.getOrDefault(risk, List.of());

            long avgWeightedWait = calculateWeightedWaitTime(riskPatients);
            long limitWithTolerance = (long) risk.getMaxMinutesWaiting() + ((risk == RiskClassification.RED) ? 5 : 0);

            boolean isAverageBreached = avgWeightedWait > limitWithTolerance;
            boolean isAnyPatientWaitingBreached = riskPatients.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.WAITING)
                    .anyMatch(a -> calculateEffectiveWaitMinutes(a) > limitWithTolerance);

            boolean slaBreached = isAverageBreached || isAnyPatientWaitingBreached;

            performances.add(new RiskAttendancePerformance(
                    risk,
                    avgWeightedWait,
                    risk.getMaxMinutesWaiting(),
                    slaBreached
            ));
        }
        return performances;
    }

    protected long calculateWeightedWaitTime(List<Attendance> patients) {
        if (patients.isEmpty()) return 0;

        double totalWeightedTime = 0.0;
        double totalWeights = 0.0;
        LocalDateTime now = LocalDateTime.now();

        for (Attendance p : patients) {
            long waitMinutes = calculateEffectiveWaitMinutes(p);

            double weight;
            if (p.getStatus() == AttendanceStatus.WAITING) {
                weight = 1.0;
            } else {
                LocalDateTime referenceTime = (p.getStartTime() != null) ? p.getStartTime() : p.getDischargeTime();
                if(referenceTime == null) referenceTime = now;

                double hoursAgo = Duration.between(referenceTime, now).toMinutes() / 60.0;
                weight = 1.0 / (1.0 + hoursAgo);
            }

            totalWeightedTime += (waitMinutes * weight);
            totalWeights += weight;
        }

        if (totalWeights == 0) return 0;
        return (long) (totalWeightedTime / totalWeights);
    }

}
