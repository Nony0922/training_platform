package com.example.service.impl;

import com.example.entity.Exam;
import com.example.entity.Score;
import com.example.entity.Student;
import com.example.exception.BusinessException;
import com.example.mapper.ExamMapper;
import com.example.mapper.ScoreMapper;
import com.example.mapper.StudentMapper;
import com.example.service.ScoreService;
import com.example.util.ExamStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScoreServiceImpl extends TeacherScopedServiceSupport implements ScoreService {
    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private ExamMapper examMapper;

    @Override public List<Score> findAll() { return scoreMapper.findAll(); }

    @Override
    public List<Score> findAllForTeacher(Integer userId, Integer teacherLevel) {
        if (isTeachingScope(teacherLevel)) {
            Set<Integer> courseIds = teachingCourseIds(userId);
            if (courseIds.isEmpty()) {
                return List.of();
            }
            Set<Integer> examIds = examMapper.findAll().stream()
                    .filter(exam -> exam.getCourseId() != null && courseIds.contains(exam.getCourseId()))
                    .map(Exam::getId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
            if (examIds.isEmpty()) {
                return List.of();
            }
            return scoreMapper.findAll().stream()
                    .filter(item -> item.getExamId() != null && examIds.contains(item.getExamId()))
                    .collect(Collectors.toList());
        }
        Set<Integer> studentIds = scopedStudentIds(studentMapper.findAll(), userId, teacherLevel);
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return scoreMapper.findAll().stream()
                .filter(item -> studentIds.contains(item.getStudentId()))
                .collect(Collectors.toList());
    }

    @Override public Score findById(Integer id) { return scoreMapper.findById(id); }

    @Override
    public int insert(Score entity) {
        validateScore(entity, null);
        return scoreMapper.insert(entity);
    }

    @Override
    public int update(Score entity) {
        validateScore(entity, entity.getId());
        return scoreMapper.update(entity);
    }

    private void validateScore(Score entity, Integer excludeId) {
        if (entity.getExamId() == null) {
            throw new BusinessException("请选择考试");
        }
        if (entity.getStudentId() == null) {
            throw new BusinessException("请选择学生");
        }
        Exam exam = examMapper.findById(entity.getExamId());
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        ExamStatusUtil.applyResolvedStatus(exam);
        if (exam.getStatus() != null && exam.getStatus() != 2) {
            throw new BusinessException("考试尚未结束，暂不可录入成绩");
        }
        Student student = studentMapper.findById(entity.getStudentId());
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        if (exam.getClassId() != null && !exam.getClassId().equals(student.getClassId())) {
            throw new BusinessException("该学生不属于本场考试班级，无法录入成绩");
        }
        int count = scoreMapper.countByExamAndStudent(entity.getExamId(), entity.getStudentId(), excludeId);
        if (count > 0) {
            throw new BusinessException("该学生本场考试成绩已存在，请勿重复录入");
        }
    }

    @Override public int deleteById(Integer id) { return scoreMapper.deleteById(id); }
}
