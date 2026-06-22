package com.example.service.impl;

import com.example.entity.AbnormalAttendance;
import com.example.entity.Attendance;
import com.example.exception.BusinessException;
import com.example.mapper.AbnormalAttendanceMapper;
import com.example.mapper.AttendanceMapper;
import com.example.service.AbnormalAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AbnormalAttendanceServiceImpl implements AbnormalAttendanceService {
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AbnormalAttendanceMapper abnormalAttendanceMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;

    @Override
    public List<AbnormalAttendance> findAll() {
        syncAllMissing();
        return abnormalAttendanceMapper.findAll();
    }

    @Override
    public int markHandled(Integer id, String handleResult, Integer handlerId) {
        if (id == null) {
            throw new BusinessException("记录ID无效");
        }
        if (!StringUtils.hasText(handleResult)) {
            throw new BusinessException("请填写处理结果");
        }
        AbnormalAttendance existing = abnormalAttendanceMapper.findById(id);
        if (existing == null) {
            throw new BusinessException("异常考勤记录不存在");
        }
        existing.setHandleStatus(1);
        existing.setHandleResult(handleResult.trim());
        existing.setHandlerId(handlerId);
        existing.setHandleTime(LocalDateTime.now().format(DATE_TIME_FMT));
        return abnormalAttendanceMapper.update(existing);
    }

    @Override
    public int deleteById(Integer id) {
        AbnormalAttendance existing = abnormalAttendanceMapper.findById(id);
        if (existing == null) {
            return 0;
        }
        if (existing.getAttendanceId() != null) {
            attendanceMapper.updateAbnormalDismissed(existing.getAttendanceId(), 1);
        }
        return abnormalAttendanceMapper.deleteById(id);
    }

    @Override
    public void syncFromAttendance(Attendance attendance) {
        if (attendance == null || attendance.getId() == null) {
            return;
        }
        Integer status = attendance.getStatus();
        AbnormalAttendance existing = abnormalAttendanceMapper.findByAttendanceId(attendance.getId());

        if (status == null || status == 1 || status == 5) {
            if (existing != null) {
                abnormalAttendanceMapper.deleteById(existing.getId());
            }
            attendanceMapper.updateAbnormalDismissed(attendance.getId(), 0);
            return;
        }

        if (status < 2 || status > 4) {
            return;
        }

        attendanceMapper.updateAbnormalDismissed(attendance.getId(), 0);

        if (existing != null) {
            if (existing.getHandleStatus() != null && existing.getHandleStatus() == 1) {
                return;
            }
            existing.setStudentId(attendance.getStudentId());
            existing.setAbnormalType(status);
            if (!StringUtils.hasText(existing.getDescription()) || existing.getDescription().startsWith("考勤自动同步")) {
                existing.setDescription(buildDescription(attendance));
            }
            abnormalAttendanceMapper.update(existing);
            return;
        }

        AbnormalAttendance abnormal = new AbnormalAttendance();
        abnormal.setAttendanceId(attendance.getId());
        abnormal.setStudentId(attendance.getStudentId());
        abnormal.setAbnormalType(status);
        abnormal.setDescription(buildDescription(attendance));
        abnormal.setHandleStatus(0);
        abnormal.setHandleResult("");
        abnormalAttendanceMapper.insert(abnormal);
    }

    @Override
    public void removeByAttendanceId(Integer attendanceId) {
        if (attendanceId != null) {
            abnormalAttendanceMapper.deleteByAttendanceId(attendanceId);
        }
    }

    private void syncAllMissing() {
        for (Attendance attendance : attendanceMapper.findAll()) {
            if (isDismissed(attendance)) {
                continue;
            }
            if (attendance.getStatus() != null
                    && attendance.getStatus() >= 2
                    && attendance.getStatus() <= 4
                    && abnormalAttendanceMapper.findByAttendanceId(attendance.getId()) == null) {
                syncFromAttendance(attendance);
            }
        }
    }

    private boolean isDismissed(Attendance attendance) {
        return attendance.getAbnormalDismissed() != null && attendance.getAbnormalDismissed() == 1;
    }

    private String buildDescription(Attendance attendance) {
        if (StringUtils.hasText(attendance.getRemark())) {
            return attendance.getRemark().trim();
        }
        String typeLabel = switch (attendance.getStatus()) {
            case 2 -> "迟到";
            case 3 -> "早退";
            case 4 -> "缺勤";
            default -> "异常";
        };
        String date = attendance.getAttendDate() != null ? attendance.getAttendDate() : "";
        return "考勤自动同步：" + date + " " + typeLabel;
    }
}
