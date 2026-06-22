package com.example.service.impl;

import com.example.entity.Attendance;
import com.example.entity.ClassSchedule;
import com.example.entity.LeaveRequest;
import com.example.entity.Student;
import com.example.exception.BusinessException;
import com.example.mapper.AttendanceMapper;
import com.example.mapper.ClassScheduleMapper;
import com.example.mapper.LeaveRequestMapper;
import com.example.mapper.StudentMapper;
import com.example.service.AbnormalAttendanceService;
import com.example.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeaveRequestServiceImpl extends TeacherScopedServiceSupport implements LeaveRequestService {
    @Autowired
    private LeaveRequestMapper leaveRequestMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private ClassScheduleMapper classScheduleMapper;
    @Autowired
    private AbnormalAttendanceService abnormalAttendanceService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String LEAVE_SYNC_REMARK = "请假审批通过自动同步";

    @Override public List<LeaveRequest> findAll() { return leaveRequestMapper.findAll(); }
    @Override
    public List<LeaveRequest> findAllForTeacher(Integer userId, Integer teacherLevel) {
        Set<Integer> studentIds = scopedStudentIds(studentMapper.findAll(), userId, teacherLevel);
        if (studentIds.isEmpty()) {
            return List.of();
        }
        return leaveRequestMapper.findAll().stream()
                .filter(item -> studentIds.contains(item.getStudentId()))
                .collect(Collectors.toList());
    }
    @Override public LeaveRequest findById(Integer id) { return leaveRequestMapper.findById(id); }
    @Override public int insert(LeaveRequest entity) { return leaveRequestMapper.insert(entity); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(LeaveRequest entity) {
        if (entity.getId() == null) {
            throw new BusinessException("请假记录无效");
        }
        LeaveRequest existing = leaveRequestMapper.findById(entity.getId());
        if (existing == null) {
            throw new BusinessException("请假记录不存在");
        }
        Integer oldStatus = existing.getStatus() != null ? existing.getStatus() : 0;
        Integer newStatus = entity.getStatus() != null ? entity.getStatus() : oldStatus;

        // 家长撤回：待审批 -> 已撤回
        if (oldStatus == 0 && newStatus == 3) {
            return leaveRequestMapper.update(entity);
        }

        // 审批：待审批 -> 通过/驳回
        if (oldStatus == 0 && (newStatus == 1 || newStatus == 2)) {
            entity.setStudentId(existing.getStudentId());
            entity.setApplicantId(existing.getApplicantId());
            entity.setApplicantName(existing.getApplicantName());
            entity.setLeaveType(existing.getLeaveType());
            entity.setStartDate(existing.getStartDate());
            entity.setEndDate(existing.getEndDate());
            entity.setReason(existing.getReason());
            int rows = leaveRequestMapper.update(entity);
            if (rows > 0 && newStatus == 1) {
                syncLeaveAttendance(entity);
            }
            return rows;
        }

        // 终态不可变更审批结果
        if (!Objects.equals(oldStatus, newStatus) && oldStatus != 0) {
            throw new BusinessException("该请假已处理，不可重复审批或修改状态");
        }

        return leaveRequestMapper.update(entity);
    }

    /**
     * 请假通过后，按「学生 + 日期 + 课程」统一写入请假考勤：
     * 1. 覆盖当日已有各课程考勤（含缺勤/迟到等）为请假，并清除异常提醒
     * 2. 按课表补全当日应上课但未录入的课程请假
     * 3. 删除旧的班级级（course_id 为空）重复记录
     */
    private void syncLeaveAttendance(LeaveRequest leave) {
        if (leave.getStudentId() == null || leave.getStartDate() == null || leave.getEndDate() == null) {
            return;
        }
        Student student = studentMapper.findById(leave.getStudentId());
        if (student == null || student.getClassId() == null) {
            return;
        }
        LocalDate start = LocalDate.parse(leave.getStartDate().substring(0, 10), DATE_FMT);
        LocalDate end = LocalDate.parse(leave.getEndDate().substring(0, 10), DATE_FMT);
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            syncLeaveAttendanceForDay(leave.getStudentId(), student.getClassId(), day);
        }
    }

    private void syncLeaveAttendanceForDay(Integer studentId, Integer classId, LocalDate day) {
        String dateStr = day.format(DATE_FMT);
        List<Attendance> existing = attendanceMapper.findByStudentAndDate(studentId, dateStr);
        Set<Integer> coveredCourseIds = new HashSet<>();

        for (Attendance record : existing) {
            if (record.getCourseId() == null) {
                continue;
            }
            coveredCourseIds.add(record.getCourseId());
            if (record.getStatus() == null || record.getStatus() != 5) {
                record.setStatus(5);
                record.setRemark(LEAVE_SYNC_REMARK);
                attendanceMapper.update(record);
            }
            abnormalAttendanceService.syncFromAttendance(record);
        }

        int weekday = day.getDayOfWeek().getValue();
        List<ClassSchedule> schedules = classScheduleMapper.findByClassIdAndWeekday(classId, weekday);
        Set<Integer> scheduledCourseIds = schedules.stream()
                .map(ClassSchedule::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        for (Integer courseId : scheduledCourseIds) {
            if (coveredCourseIds.contains(courseId)) {
                continue;
            }
            Attendance record = new Attendance();
            record.setStudentId(studentId);
            record.setClassId(classId);
            record.setCourseId(courseId);
            record.setAttendDate(dateStr);
            record.setStatus(5);
            record.setRemark(LEAVE_SYNC_REMARK);
            attendanceMapper.insert(record);
            abnormalAttendanceService.syncFromAttendance(record);
            coveredCourseIds.add(courseId);
        }

        for (Attendance record : existing) {
            if (record.getCourseId() == null && record.getId() != null) {
                abnormalAttendanceService.removeByAttendanceId(record.getId());
            }
        }
        attendanceMapper.deleteClassLevelByStudentAndDate(studentId, dateStr);
    }

    @Override public int deleteById(Integer id) { return leaveRequestMapper.deleteById(id); }
}
