package com.study.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.platform.entity.Course;
import com.study.platform.entity.CourseSelection;
import com.study.platform.entity.Reservation;
import com.study.platform.entity.User;
import com.study.platform.repository.CourseRepository;
import com.study.platform.repository.CourseSelectionRepository;
import com.study.platform.repository.ReservationRepository;
import com.study.platform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiContextService {

    // 负责把“当前用户相关的平台数据”拼成 JSON，注入到 AI 对话上下文里
    private final CourseRepository courseRepository;
    private final CourseSelectionRepository selectionRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AiContextService(CourseRepository courseRepository,
                            CourseSelectionRepository selectionRepository,
                            ReservationRepository reservationRepository,
                            UserRepository userRepository,
                            ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.selectionRepository = selectionRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public String buildForUser(Long userId, String role) {
        // 取全部课程作为可选项
        List<Course> allCourses = courseRepository.findAll();
        Map<Long, String> courseNames = allCourses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getName, (a, b) -> a));

        List<Map<String, Object>> available = allCourses.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("code", c.getCode());
            m.put("name", c.getName());
            m.put("teacher", c.getTeacher());
            m.put("schedule", c.getSchedule());
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> selected = new ArrayList<>();
        List<Map<String, Object>> reservations = new ArrayList<>();

        if ("admin".equals(role)) {
            // 管理员：给全部课程 + 全部预约（带学生姓名）
            for (Reservation r : reservationRepository.findAll()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("courseName", courseNames.get(r.getCourseId()));
                m.put("studentName", userName(r.getStudentId()));
                m.put("date", String.valueOf(r.getReserveDate()));
                m.put("timeSlot", r.getTimeSlot());
                m.put("status", r.getStatus());
                reservations.add(m);
            }
        } else {
            // 学生：给全部课程 + 自己的已选课程 + 自己的预约
            for (CourseSelection s : selectionRepository.findByStudentId(userId)) {
                allCourses.stream()
                        .filter(c -> c.getId().equals(s.getCourseId()))
                        .findFirst()
                        .ifPresent(c -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("id", c.getId());
                            m.put("name", c.getName());
                            m.put("teacher", c.getTeacher());
                            m.put("schedule", c.getSchedule());
                            selected.add(m);
                        });
            }
            for (Reservation r : reservationRepository.findByStudentIdOrderByIdDesc(userId)) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("courseName", courseNames.get(r.getCourseId()));
                m.put("date", String.valueOf(r.getReserveDate()));
                m.put("timeSlot", r.getTimeSlot());
                m.put("status", r.getStatus());
                reservations.add(m);
            }
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("availableCourses", available);
        root.put("selectedCourses", selected);
        root.put("reservations", reservations);

        try {
            String json = objectMapper.writeValueAsString(root);
            // 系统提示词：明确要求模型基于真实数据回答、不编造
            return "你是校园 AI 助教。以下是当前用户相关的平台真实数据（JSON），请基于这些数据回答用户问题；"
                    + "如果用户询问的信息不在这些数据里，请如实说明，不要编造。\n" + json;
        } catch (JsonProcessingException e) {
            return "你是校园 AI 助教。";
        }
    }

    private String userName(Long id) {
        return userRepository.findById(id).map(User::getName).orElse("ID:" + id);
    }
}
