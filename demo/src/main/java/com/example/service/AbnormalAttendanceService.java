package com.example.service;

import com.example.entity.AbnormalAttendance;
import com.example.entity.Attendance;
import java.util.List;

public interface AbnormalAttendanceService {
    List<AbnormalAttendance> findAll();
    int deleteById(Integer id);
    /** 处理异常：标记为已处理并写入处理结果 */
    int markHandled(Integer id, String handleResult, Integer handlerId);
    /** 根据考勤记录同步异常考勤（迟到/早退/缺勤写入，正常/请假移除） */
    void syncFromAttendance(Attendance attendance);
    void removeByAttendanceId(Integer attendanceId);
}
