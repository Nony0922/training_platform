package com.example.mapper;

import com.example.entity.StudentCourseEnrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StudentCourseEnrollmentMapper {
    List<StudentCourseEnrollment> findAll();
    StudentCourseEnrollment findById(Integer id);
    StudentCourseEnrollment findActiveByStudentAndCourse(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);
    int countActiveByParentAndCourse(@Param("parentId") Integer parentId, @Param("courseId") Integer courseId);
    int insert(StudentCourseEnrollment entity);
    int deactivateByOrderId(@Param("orderId") Integer orderId);
}
