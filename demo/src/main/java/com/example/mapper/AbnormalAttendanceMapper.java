package com.example.mapper;

import com.example.entity.AbnormalAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AbnormalAttendanceMapper {
    List<AbnormalAttendance> findAll();
    AbnormalAttendance findById(Integer id);
    AbnormalAttendance findByAttendanceId(@Param("attendanceId") Integer attendanceId);
    int insert(AbnormalAttendance entity);
    int update(AbnormalAttendance entity);
    int deleteById(Integer id);
    int deleteByAttendanceId(@Param("attendanceId") Integer attendanceId);
}
