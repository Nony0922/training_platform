package com.example.service.impl;

import com.example.entity.TeachingProgress;
import com.example.mapper.TeachingProgressMapper;
import com.example.service.TeachingProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeachingProgressServiceImpl extends TeacherScopedServiceSupport implements TeachingProgressService {
    @Autowired
    private TeachingProgressMapper teachingProgressMapper;

    @Override public List<TeachingProgress> findAll() { return teachingProgressMapper.findAll(); }

    @Override
    public List<TeachingProgress> findAllForTeacher(Integer userId, Integer teacherLevel) {
        if (isTeachingScope(teacherLevel)) {
            Set<Integer> courseIds = teachingCourseIds(userId);
            if (courseIds.isEmpty()) {
                return List.of();
            }
            return teachingProgressMapper.findAll().stream()
                    .filter(p -> p.getCourseId() != null && courseIds.contains(p.getCourseId()))
                    .collect(Collectors.toList());
        }
        List<Integer> classIds = classIds(userId, teacherLevel);
        if (classIds.isEmpty()) {
            return List.of();
        }
        return teachingProgressMapper.findAll().stream()
                .filter(p -> p.getClassId() != null && classIds.contains(p.getClassId()))
                .collect(Collectors.toList());
    }

    @Override
    public String validateForTeacher(TeachingProgress entity, Integer userId, Integer teacherLevel, boolean isUpdate) {
        if (!isTeachingScope(teacherLevel)) {
            return "仅支持管理本人授课课程的教学进度";
        }
        Integer teacherId = teacherScopeService.resolveTeacherId(userId);
        if (teacherId == null) {
            return "教师信息不存在";
        }
        Set<Integer> courseIds = teachingCourseIds(userId);
        if (entity.getCourseId() == null || !courseIds.contains(entity.getCourseId())) {
            return "只能管理本人授课课程的教学进度";
        }
        entity.setTeacherId(teacherId);
        if (isUpdate && entity.getId() != null) {
            TeachingProgress existing = teachingProgressMapper.findById(entity.getId());
            if (existing == null || existing.getCourseId() == null || !courseIds.contains(existing.getCourseId())) {
                return "无权修改该教学进度";
            }
        }
        return null;
    }

    @Override
    public boolean canDeleteForTeacher(Integer id, Integer userId, Integer teacherLevel) {
        if (!isTeachingScope(teacherLevel)) {
            return false;
        }
        TeachingProgress existing = teachingProgressMapper.findById(id);
        if (existing == null) {
            return false;
        }
        Set<Integer> courseIds = teachingCourseIds(userId);
        return existing.getCourseId() != null && courseIds.contains(existing.getCourseId());
    }

    @Override public TeachingProgress findById(Integer id) { return teachingProgressMapper.findById(id); }
    @Override public int insert(TeachingProgress entity) { return teachingProgressMapper.insert(entity); }
    @Override public int update(TeachingProgress entity) { return teachingProgressMapper.update(entity); }
    @Override public int deleteById(Integer id) { return teachingProgressMapper.deleteById(id); }
}
