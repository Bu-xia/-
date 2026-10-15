package com.study.platform.controller;

import com.study.platform.dto.ReservationRequest;
import com.study.platform.entity.Reservation;
import com.study.platform.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    // 课程预约：学生预约/取消，管理员查看全部并确认状态
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public Map<String, Object> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        List<Map<String, Object>> list = "admin".equals(role)
                ? reservationService.all()
                : reservationService.mine(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return result;
    }

    @PostMapping
    public Reservation create(@RequestBody ReservationRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return reservationService.create(req, userId);
    }

    @PutMapping("/{id}/status")
    public Reservation updateStatus(@PathVariable Long id,
                                    @RequestBody Map<String, String> body,
                                    HttpServletRequest request) {
        if (!"admin".equals(request.getAttribute("role"))) {
            throw new RuntimeException("没有权限，仅管理员可确认");
        }
        return reservationService.updateStatus(id, body.get("status"));
    }

    @PostMapping("/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        reservationService.cancel(id, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        return result;
    }
}
