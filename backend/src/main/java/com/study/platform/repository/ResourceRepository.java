package com.study.platform.repository;

import com.study.platform.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Query("SELECT r FROM Resource r WHERE " +
            "(:keyword IS NULL OR r.title LIKE %:keyword% OR r.tags LIKE %:keyword% OR r.description LIKE %:keyword%) " +
            "AND (:category IS NULL OR r.category = :category) " +
            "AND (:courseId IS NULL OR r.courseId = :courseId)")
    List<Resource> search(@Param("keyword") String keyword,
                          @Param("category") String category,
                          @Param("courseId") Long courseId);
}
