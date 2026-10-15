package com.study.platform.service;

import com.study.platform.entity.Resource;
import com.study.platform.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    // 资料检索与上传，文件保存到本地 uploads 目录
    private final ResourceRepository resourceRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<Resource> search(String keyword, String category, Long courseId) {
        return resourceRepository.search(
                keyword == null || keyword.isBlank() ? null : keyword.trim(),
                category == null || category.isBlank() ? null : category,
                courseId);
    }

    public Resource upload(String title, String category, Long courseId, String description,
                           String tags, MultipartFile file, Long uploaderId) {
        // 有文件时生成 UUID 文件名保存，避免重名覆盖
        if (title == null || title.isBlank()) {
            throw new RuntimeException("请填写资料标题");
        }
        String fileUrl = "";
        String fileType = "";
        if (file != null && !file.isEmpty()) {
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            int dot = original.lastIndexOf('.');
            String ext = dot >= 0 ? original.substring(dot) : "";
            fileType = ext.replace(".", "");
            String name = UUID.randomUUID().toString().replace("-", "") + ext;
            try {
                Path dir = Paths.get(uploadDir).toAbsolutePath();
                Files.createDirectories(dir);
                file.transferTo(dir.resolve(name).toFile());
                fileUrl = "/uploads/" + name;
            } catch (IOException e) {
                throw new RuntimeException("文件保存失败：" + e.getMessage());
            }
        }
        Resource resource = new Resource();
        resource.setTitle(title.trim());
        resource.setCategory(category);
        resource.setCourseId(courseId);
        resource.setDescription(description);
        resource.setTags(tags);
        resource.setFileUrl(fileUrl);
        resource.setFileType(fileType);
        resource.setUploaderId(uploaderId);
        return resourceRepository.save(resource);
    }

    public void delete(Long id) {
        resourceRepository.deleteById(id);
    }
}
