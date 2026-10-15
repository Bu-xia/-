package com.study.platform.repository;

import com.study.platform.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByNameContainingIgnoreCaseOrTeacherContainingIgnoreCase(String name, String teacher);
}
