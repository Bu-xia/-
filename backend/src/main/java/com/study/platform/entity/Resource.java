package com.study.platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    // 电子资料：fileUrl 保存上传文件的可访问地址
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String category;

    private Long courseId;

    @Column(length = 500)
    private String fileUrl;

    @Column(length = 50)
    private String fileType;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String tags;

    private Long uploaderId;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
