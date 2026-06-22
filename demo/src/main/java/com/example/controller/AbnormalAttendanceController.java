package com.example.controller;

import com.example.entity.AbnormalAttendance;
import com.example.service.AbnormalAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/abnormal-attendance")
@CrossOrigin
public class AbnormalAttendanceController {
    @Autowired
    private AbnormalAttendanceService abnormalAttendanceService;

    @GetMapping("/list")
    public List<AbnormalAttendance> findAll() {
        return abnormalAttendanceService.findAll();
    }

    @PutMapping("/{id:\\d+}/handle")
    public Map<String, Object> markHandled(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String handleResult = body.get("handleResult") != null ? String.valueOf(body.get("handleResult")) : null;
            Integer handlerId = null;
            if (body.get("handlerId") != null) {
                handlerId = ((Number) body.get("handlerId")).intValue();
            }
            int r = abnormalAttendanceService.markHandled(id, handleResult, handlerId);
            result.put("code", r > 0 ? 200 : 500);
            result.put("msg", r > 0 ? "处理成功" : "处理失败");
        } catch (RuntimeException e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id:\\d+}")
    public Map<String, Object> deleteById(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            int r = abnormalAttendanceService.deleteById(id);
            result.put("code", r > 0 ? 200 : 500);
            result.put("msg", r > 0 ? "删除成功" : "删除失败");
        } catch (RuntimeException e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
