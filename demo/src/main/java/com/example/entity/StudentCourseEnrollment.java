package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseEnrollment {
    private Integer id;
    private Integer studentId;
    private Integer courseId;
    private Integer orderId;
    private Integer parentId;
    private Integer status;
    private String enrollTime;
    private String createTime;
    private String studentName;
    private String courseName;
}
