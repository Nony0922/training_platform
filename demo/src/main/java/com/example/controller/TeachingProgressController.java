package com.example.controller;

import com.example.entity.TeachingProgress;
import com.example.service.TeachingProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/progress")
@CrossOrigin
public class TeachingProgressController {
    @Autowired
    private TeachingProgressService teachingProgressService;

    @GetMapping("/list")
    public List<TeachingProgress> findAll(@RequestParam(required = false) Integer scopeUserId,
                                          @RequestParam(required = false) Integer teacherLevel) {
        if (scopeUserId != null && teacherLevel != null) {
            return teachingProgressService.findAllForTeacher(scopeUserId, teacherLevel);
        }
        return teachingProgressService.findAll();
    }

    @GetMapping("/{id}")
    public TeachingProgress findById(@PathVariable Integer id) { return teachingProgressService.findById(id); }

    @PostMapping("/add")
    public Map<String, Object> insert(@RequestBody TeachingProgress entity,
                                      @RequestParam(required = false) Integer scopeUserId,
                                      @RequestParam(required = false) Integer teacherLevel) {
        Map<String, Object> result = new HashMap<>();
        if (scopeUserId != null && teacherLevel != null) {
            String err = teachingProgressService.validateForTeacher(entity, scopeUserId, teacherLevel, false);
            if (err != null) {
                result.put("code", 500);
                result.put("msg", err);
                return result;
            }
        }
        int r = teachingProgressService.insert(entity);
        result.put("code", r > 0 ? 200 : 500);
        result.put("msg", r > 0 ? "添加成功" : "添加失败");
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody TeachingProgress entity,
                                      @RequestParam(required = false) Integer scopeUserId,
                                      @RequestParam(required = false) Integer teacherLevel) {
        Map<String, Object> result = new HashMap<>();
        if (scopeUserId != null && teacherLevel != null) {
            String err = teachingProgressService.validateForTeacher(entity, scopeUserId, teacherLevel, true);
            if (err != null) {
                result.put("code", 500);
                result.put("msg", err);
                return result;
            }
        }
        int r = teachingProgressService.update(entity);
        result.put("code", r > 0 ? 200 : 500);
        result.put("msg", r > 0 ? "更新成功" : "更新失败");
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteById(@PathVariable Integer id,
                                          @RequestParam(required = false) Integer scopeUserId,
                                          @RequestParam(required = false) Integer teacherLevel) {
        Map<String, Object> result = new HashMap<>();
        if (scopeUserId != null && teacherLevel != null) {
            if (!teachingProgressService.canDeleteForTeacher(id, scopeUserId, teacherLevel)) {
                result.put("code", 500);
                result.put("msg", "无权删除该教学进度");
                return result;
            }
        }
        int r = teachingProgressService.deleteById(id);
        result.put("code", r > 0 ? 200 : 500);
        result.put("msg", r > 0 ? "删除成功" : "删除失败");
        return result;
    }
}
