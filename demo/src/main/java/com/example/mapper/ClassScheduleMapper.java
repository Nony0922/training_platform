package com.example.mapper;

import com.example.entity.ClassSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ClassScheduleMapper {
    List<ClassSchedule> findAll();
    ClassSchedule findById(Integer id);
    List<ClassSchedule> findBySemester(@Param("semester") String semester);
    List<String> findSemesters();
    List<Integer> findClassIdsByTeacherId(@Param("teacherId") Integer teacherId);
    List<ClassSchedule> findByTeacherId(@Param("teacherId") Integer teacherId, @Param("semester") String semester);
    List<ClassSchedule> findByTeacherUserId(@Param("userId") Integer userId, @Param("semester") String semester);
    List<ClassSchedule> findByClassIdAndWeekday(@Param("classId") Integer classId, @Param("weekday") Integer weekday);
    int insert(ClassSchedule entity);
    int update(ClassSchedule entity);
    int deleteById(Integer id);
}
