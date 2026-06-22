package com.example.service;

import com.example.entity.Course;
import java.util.List;

public interface CourseService {
    List<Course> findAll();
    List<Course> findAllForTeacher(Integer userId, Integer teacherLevel, Integer scopeTeacherId);
    Course findById(Integer id);
    int insert(Course entity);
    int update(Course entity);
    int deleteById(Integer id);
    int incrementEnrolledCount(Integer id);
    int decrementEnrolledCount(Integer id);
    String validatePurchasable(Course course);
    String validateGradeForParent(Course course, Integer parentId);
}
