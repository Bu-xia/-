package com.study.platform.controller;

import com.study.platform.entity.Resource;
import com.study.platform.service.ResourceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    // 课程电子资料库：检索与文件上传
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public Map<String, Object> search(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) Long courseId) {
        Map<String, Object> result = new HashMap<>();
        List<Resource> list = resourceService.search(keyword, category, courseId);
        result.put("list", list);
        return result;
    }

    @PostMapping
    public Resource upload(@RequestParam("title") String title,
                           @RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "courseId", required = false) Long courseId,
                           @RequestParam(value = "description", required = false) String description,
                           @RequestParam(value = "tags", required = false) String tags,
                           @RequestParam(value = "file", required = false) MultipartFile file,
                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return resourceService.upload(title, category, courseId, description, tags, file, userId);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!"admin".equals(request.getAttribute("role"))) {
            throw new RuntimeException("没有权限，仅管理员可删除");
        }
        resourceService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        return result;
    }
}
