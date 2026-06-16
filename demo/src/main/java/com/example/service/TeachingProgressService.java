package com.example.service;

import com.example.entity.TeachingProgress;
import java.util.List;

public interface TeachingProgressService {
    List<TeachingProgress> findAll();
    List<TeachingProgress> findAllForTeacher(Integer userId, Integer teacherLevel);
    TeachingProgress findById(Integer id);
    String validateForTeacher(TeachingProgress entity, Integer userId, Integer teacherLevel, boolean isUpdate);
    boolean canDeleteForTeacher(Integer id, Integer userId, Integer teacherLevel);
    int insert(TeachingProgress entity);
    int update(TeachingProgress entity);
    int deleteById(Integer id);
}
