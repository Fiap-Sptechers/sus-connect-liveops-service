package com.fiap.sus.liveops.modules.attendance.repository;

import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.shared.enums.AttendanceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {

    List<Attendance> findByHealthUnitIdAndStatus(String healthUnitId, AttendanceStatus attendanceStatus);

    List<Attendance> findByHealthUnitIdAndStatusNot(String healthUnitId, AttendanceStatus attendanceStatus);

    List<Attendance> findByHealthUnitIdAndEntryTimeAfter(String healthUnitId, LocalDateTime cutoffDate);

}
