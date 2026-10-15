package com.study.platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_selections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"studentId", "courseId"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSelection {
    // 选课关系：同一学生同一课程只允许一条记录
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private Long courseId;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
