package com.fiap.sus.liveops.modules.attendance.repository;

import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
}
