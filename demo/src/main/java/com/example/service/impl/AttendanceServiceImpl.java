package com.example.service.impl;

import com.example.entity.Attendance;
import com.example.exception.BusinessException;
import com.example.mapper.AttendanceMapper;
import com.example.service.AbnormalAttendanceService;
import com.example.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl extends TeacherScopedServiceSupport implements AttendanceService {
    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private AbnormalAttendanceService abnormalAttendanceService;

    @Override public List<Attendance> findAll() { return attendanceMapper.findAll(); }

    @Override
    public List<Attendance> findAllForTeacher(Integer userId, Integer teacherLevel) {
        if (isTeachingScope(teacherLevel)) {
            Set<Integer> courseIds = teachingCourseIds(userId);
            if (courseIds.isEmpty()) {
                return List.of();
            }
            return attendanceMapper.findAll().stream()
                    .filter(item -> item.getCourseId() != null && courseIds.contains(item.getCourseId()))
                    .collect(Collectors.toList());
        }
        List<Integer> classIds = classIds(userId, teacherLevel);
        if (classIds.isEmpty()) {
            return List.of();
        }
        return attendanceMapper.findAll().stream()
                .filter(item -> item.getClassId() != null && classIds.contains(item.getClassId()))
                .collect(Collectors.toList());
    }

    @Override public Attendance findById(Integer id) { return attendanceMapper.findById(id); }

    @Override
    public int insert(Attendance entity) {
        validateAttendance(entity, null);
        int rows = attendanceMapper.insert(entity);
        if (rows > 0) {
            abnormalAttendanceService.syncFromAttendance(entity);
        }
        return rows;
    }

    @Override
    public int update(Attendance entity) {
        validateAttendance(entity, entity.getId());
        int rows = attendanceMapper.update(entity);
        if (rows > 0) {
            abnormalAttendanceService.syncFromAttendance(entity);
        }
        return rows;
    }

    private void validateAttendance(Attendance entity, Integer excludeId) {
        if (entity.getStudentId() == null) {
            throw new BusinessException("请选择学生");
        }
        if (entity.getAttendDate() == null || entity.getAttendDate().isBlank()) {
            throw new BusinessException("请填写考勤日期");
        }
        int count = attendanceMapper.countByStudentCourseDate(
                entity.getStudentId(), entity.getCourseId(), entity.getAttendDate(), excludeId);
        if (count > 0) {
            throw new BusinessException("该学生在此日期已有考勤记录，请勿重复录入");
        }
    }

    @Override
    public int deleteById(Integer id) {
        abnormalAttendanceService.removeByAttendanceId(id);
        return attendanceMapper.deleteById(id);
    }
}
