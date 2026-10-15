package com.study.platform.controller;

import com.study.platform.entity.Course;
import com.study.platform.service.CourseService;
import com.study.platform.service.CourseSelectionService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    // 课程管理接口 + 选课/退选/我的课程接口
    private final CourseService courseService;
    private final CourseSelectionService courseSelectionService;

    public CourseController(CourseService courseService,
                            CourseSelectionService courseSelectionService) {
        this.courseService = courseService;
        this.courseSelectionService = courseSelectionService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        List<Course> list = courseService.list(keyword);
        result.put("list", list);
        return result;
    }

    @GetMapping("/{id}")
    public Course get(@PathVariable Long id) {
        return courseService.get(id);
    }

    @GetMapping("/my")
    public Map<String, Object> myCourses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("list", courseSelectionService.myCourses(userId));
        result.put("selectedIds", courseSelectionService.myCourseIds(userId));
        return result;
    }

    @PostMapping("/{id}/select")
    public Map<String, Object> select(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        courseSelectionService.select(id, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        return result;
    }

    @PostMapping("/{id}/deselect")
    public Map<String, Object> deselect(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        courseSelectionService.deselect(id, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        return result;
    }

    @PostMapping
    public Course create(@RequestBody Course course, HttpServletRequest request) {
        requireAdmin(request);
        course.setId(null);
        return courseService.save(course);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course course, HttpServletRequest request) {
        requireAdmin(request);
        Course existing = courseService.get(id);
        existing.setCode(course.getCode());
        existing.setName(course.getName());
        existing.setTeacher(course.getTeacher());
        existing.setDescription(course.getDescription());
        existing.setSchedule(course.getSchedule());
        existing.setCapacity(course.getCapacity());
        return courseService.save(existing);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        courseService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        return result;
    }

    private void requireAdmin(HttpServletRequest request) {
        if (!"admin".equals(request.getAttribute("role"))) {
            throw new RuntimeException("没有权限，仅管理员可操作");
        }
    }
}
