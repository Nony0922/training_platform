package com.example.mapper;

import com.example.entity.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttendanceMapper {
    List<Attendance> findAll();
    Attendance findById(Integer id);
    int insert(Attendance entity);
    int update(Attendance entity);
    int updateAbnormalDismissed(@Param("id") Integer id, @Param("abnormalDismissed") Integer abnormalDismissed);
    int deleteById(Integer id);
    Attendance findByStudentClassDate(Integer studentId, Integer classId, String attendDate);
    List<Attendance> findByStudentAndDate(@Param("studentId") Integer studentId, @Param("attendDate") String attendDate);
    int deleteClassLevelByStudentAndDate(@Param("studentId") Integer studentId, @Param("attendDate") String attendDate);
    int countByStudentCourseDate(Integer studentId, Integer courseId, String attendDate, Integer excludeId);
}
