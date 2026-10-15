package com.study.platform.service;

import com.study.platform.dto.ReservationRequest;
import com.study.platform.entity.Course;
import com.study.platform.entity.Reservation;
import com.study.platform.entity.User;
import com.study.platform.repository.CourseRepository;
import com.study.platform.repository.ReservationRepository;
import com.study.platform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    // 预约业务：创建、取消、状态流转，返回时附带课程名与学生名
    private final ReservationRepository reservationRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              CourseRepository courseRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public Reservation create(ReservationRequest req, Long studentId) {
        // 每个学生同一门课只允许一个“待确认”的预约，避免重复占位
        if (req.getCourseId() == null || req.getReserveDate() == null) {
            throw new RuntimeException("请选择课程和日期");
        }
        if (reservationRepository.existsByCourseIdAndStudentIdAndStatus(
                req.getCourseId(), studentId, "pending")) {
            throw new RuntimeException("该课程已有待确认的预约");
        }
        Reservation reservation = new Reservation();
        reservation.setCourseId(req.getCourseId());
        reservation.setStudentId(studentId);
        reservation.setReserveDate(req.getReserveDate());
        reservation.setTimeSlot(req.getTimeSlot());
        reservation.setStatus("pending");
        return reservationRepository.save(reservation);
    }

    public List<Map<String, Object>> mine(Long studentId) {
        return enrich(reservationRepository.findByStudentIdOrderByIdDesc(studentId));
    }

    public List<Map<String, Object>> all() {
        return enrich(reservationRepository.findAll());
    }

    public Reservation updateStatus(Long id, String status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }

    public void cancel(Long id, Long studentId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        if (!reservation.getStudentId().equals(studentId)) {
            throw new RuntimeException("只能取消自己的预约");
        }
        reservation.setStatus("cancelled");
        reservationRepository.save(reservation);
    }

    private List<Map<String, Object>> enrich(List<Reservation> reservations) {
        Map<Long, String> courseNames = new HashMap<>();
        Map<Long, String> userNames = new HashMap<>();
        for (Reservation r : reservations) {
            if (r.getCourseId() != null && !courseNames.containsKey(r.getCourseId())) {
                courseRepository.findById(r.getCourseId())
                        .map(Course::getName)
                        .ifPresent(n -> courseNames.put(r.getCourseId(), n));
            }
            if (r.getStudentId() != null && !userNames.containsKey(r.getStudentId())) {
                userRepository.findById(r.getStudentId())
                        .map(User::getName)
                        .ifPresent(n -> userNames.put(r.getStudentId(), n));
            }
        }
        return reservations.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("courseId", r.getCourseId());
            m.put("courseName", courseNames.get(r.getCourseId()));
            m.put("studentId", r.getStudentId());
            m.put("studentName", userNames.get(r.getStudentId()));
            m.put("reserveDate", r.getReserveDate());
            m.put("timeSlot", r.getTimeSlot());
            m.put("status", r.getStatus());
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }
}
