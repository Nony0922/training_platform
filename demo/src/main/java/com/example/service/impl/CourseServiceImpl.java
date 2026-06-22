package com.example.service.impl;

import com.example.entity.Course;
import com.example.entity.Student;
import com.example.entity.Teacher;
import com.example.exception.BusinessException;
import com.example.mapper.CourseMapper;
import com.example.mapper.StudentMapper;
import com.example.mapper.TeacherMapper;
import com.example.service.CourseService;
import com.example.util.GradeMatchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends TeacherScopedServiceSupport implements CourseService {
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    @Override public List<Course> findAll() { return courseMapper.findAll(); }

    @Override
    public List<Course> findAllForTeacher(Integer userId, Integer teacherLevel, Integer scopeTeacherId) {
        if (userId != null) {
            return courseMapper.findByTeacherUserId(userId);
        }
        Integer teacherId = teacherScopeService.resolveTeacherId(userId, scopeTeacherId);
        if (teacherId == null) {
            return List.of();
        }
        return courseMapper.findByTeacherId(teacherId);
    }

    @Override public Course findById(Integer id) { return courseMapper.findById(id); }

    @Override
    public int insert(Course entity) {
        validateCourse(entity, false);
        return courseMapper.insert(entity);
    }

    @Override
    public int update(Course entity) {
        validateCourse(entity, true);
        return courseMapper.update(entity);
    }

    private void validateCourse(Course entity, boolean isUpdate) {
        if (entity == null || !org.springframework.util.StringUtils.hasText(entity.getName())) {
            throw new BusinessException("请填写课程名称");
        }
        if (entity.getTeacherId() == null) {
            throw new BusinessException("请选择任课教师，否则教师端无法查看该课程");
        }
        Teacher assigned = teacherMapper.findById(entity.getTeacherId());
        if (assigned == null) {
            throw new BusinessException("任课教师不存在");
        }
        if (assigned.getUserId() == null) {
            throw new BusinessException("该教师未绑定登录账号，请先在权限管理中创建对应教师账号");
        }
        if (isUpdate && entity.getId() == null) {
            throw new BusinessException("课程ID无效");
        }
    }

    @Override public int deleteById(Integer id) { return courseMapper.deleteById(id); }
    @Override public int incrementEnrolledCount(Integer id) { return courseMapper.incrementEnrolledCount(id); }
    @Override public int decrementEnrolledCount(Integer id) { return courseMapper.decrementEnrolledCount(id); }

    @Override
    public String validateGradeForParent(Course course, Integer parentId) {
        if (course == null || parentId == null) {
            return "参数无效";
        }
        List<Student> children = studentMapper.findAll().stream()
                .filter(s -> parentId.equals(s.getParentId()))
                .collect(Collectors.toList());
        if (children.isEmpty()) {
            return "请先关联子女信息后再购买课程";
        }
        boolean matched = children.stream()
                .anyMatch(s -> GradeMatchUtil.isGradeMatch(course.getTargetGrade(), s.getClassName()));
        if (!matched) {
            return "该课程适用年级与您的孩子不匹配，无法购买";
        }
        return null;
    }

    @Override
    public String validatePurchasable(Course course) {
        if (course == null) {
            return "课程不存在";
        }
        if (course.getStatus() == null || course.getStatus() != 1) {
            return "课程已下架，无法购买";
        }
        LocalDate today = LocalDate.now();
        if (course.getValidEnd() != null && !course.getValidEnd().isEmpty()) {
            if (LocalDate.parse(course.getValidEnd()).isBefore(today)) {
                return "课程已结束报名";
            }
        }
        if (course.getValidStart() != null && !course.getValidStart().isEmpty()) {
            if (LocalDate.parse(course.getValidStart()).isAfter(today)) {
                return "课程尚未开放报名";
            }
        }
        if (course.getMaxStudents() != null && course.getMaxStudents() > 0
                && course.getEnrolledCount() != null
                && course.getEnrolledCount() >= course.getMaxStudents()) {
            return "课程名额已满";
        }
        return null;
    }
}
