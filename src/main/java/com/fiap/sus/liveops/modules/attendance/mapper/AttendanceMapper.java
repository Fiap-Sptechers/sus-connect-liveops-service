package com.fiap.sus.liveops.modules.attendance.mapper;

import com.fiap.sus.liveops.modules.attendance.document.Attendance;
import com.fiap.sus.liveops.modules.attendance.dto.AttendanceResponse;
import com.fiap.sus.liveops.modules.attendance.dto.CompleteAttendanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(source = "patient.name", target = "patientName")
    AttendanceResponse toResponse(Attendance entity);

    @Mapping(source = "patient.name", target = "patientName")
    CompleteAttendanceResponse toCompleteResponse(Attendance entity);

}
