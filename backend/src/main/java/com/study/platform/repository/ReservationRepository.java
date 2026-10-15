package com.study.platform.repository;

import com.study.platform.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStudentIdOrderByIdDesc(Long studentId);
    boolean existsByCourseIdAndStudentIdAndStatus(Long courseId, Long studentId, String status);
}
