package com.study.platform.service;

import com.study.platform.entity.Course;
import com.study.platform.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    // 课程的增删改查与关键字搜索
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> list(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return courseRepository.findAll();
        }
        return courseRepository.findByNameContainingIgnoreCaseOrTeacherContainingIgnoreCase(keyword.trim(), keyword.trim());
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public Course get(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("课程不存在"));
    }

    public void delete(Long id) {
        courseRepository.deleteById(id);
    }
}
