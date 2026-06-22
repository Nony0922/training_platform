package com.example.service.impl;

import com.example.entity.ClassSchedule;
import com.example.entity.Course;
import com.example.exception.BusinessException;
import com.example.mapper.ClassScheduleMapper;
import com.example.mapper.CourseMapper;
import com.example.service.ClassScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class ClassScheduleServiceImpl extends TeacherScopedServiceSupport implements ClassScheduleService {
    @Autowired
    private ClassScheduleMapper classScheduleMapper;
    @Autowired
    private CourseMapper courseMapper;

    @Override public List<ClassSchedule> findAll() { return classScheduleMapper.findAll(); }

    @Override
    public List<ClassSchedule> findAllForTeacher(Integer userId, Integer teacherLevel, String semester, Integer scopeTeacherId) {
        if (userId != null) {
            return classScheduleMapper.findByTeacherUserId(userId, semester);
        }
        Integer teacherId = teacherScopeService.resolveTeacherId(userId, scopeTeacherId);
        if (teacherId == null) {
            return List.of();
        }
        return classScheduleMapper.findByTeacherId(teacherId, semester);
    }

    @Override public List<String> findSemesters() { return classScheduleMapper.findSemesters(); }
    @Override public ClassSchedule findById(Integer id) { return classScheduleMapper.findById(id); }

    @Override
    public int insert(ClassSchedule entity) {
        validateSchedule(entity);
        return classScheduleMapper.insert(entity);
    }

    @Override
    public int update(ClassSchedule entity) {
        validateSchedule(entity);
        return classScheduleMapper.update(entity);
    }

    private void validateSchedule(ClassSchedule entity) {
        if (entity.getCourseId() == null) {
            throw new BusinessException("请选择课程");
        }
        if (entity.getTeacherId() == null) {
            throw new BusinessException("请选择授课教师");
        }
        Course course = courseMapper.findById(entity.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (!Objects.equals(course.getTeacherId(), entity.getTeacherId())) {
            throw new BusinessException("授课教师须与课程任课教师一致，教师端课表才能正确显示");
        }
    }

    @Override public int deleteById(Integer id) { return classScheduleMapper.deleteById(id); }
}
