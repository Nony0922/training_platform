package com.example.mapper;

import com.example.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CourseMapper {
    List<Course> findAll();
    Course findById(Integer id);
    List<Course> findByTeacherId(@Param("teacherId") Integer teacherId);
    List<Course> findByTeacherUserId(@Param("userId") Integer userId);
    int insert(Course entity);
    int update(Course entity);
    int deleteById(Integer id);
    int incrementEnrolledCount(Integer id);
    int decrementEnrolledCount(Integer id);
}
