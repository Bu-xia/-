package com.study.platform.repository;

import com.study.platform.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    List<CourseSelection> findByStudentId(Long studentId);
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}
