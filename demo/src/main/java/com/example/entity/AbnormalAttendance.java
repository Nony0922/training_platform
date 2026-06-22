package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalAttendance {
    private Integer id;
    private Integer attendanceId;
    private Integer studentId;
    private Integer abnormalType;
    private String description;
    private Integer handleStatus;
    private String handleResult;
    private Integer handlerId;
    private String handleTime;
    private String createTime;
    private String studentName;
    /** 关联考勤日期（查询展示） */
    private String attendDate;
    /** 关联课程名称（查询展示） */
    private String courseName;
}
