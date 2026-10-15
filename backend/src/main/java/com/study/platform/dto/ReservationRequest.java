package com.study.platform.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationRequest {
    private Long courseId;
    private LocalDate reserveDate;
    private String timeSlot;
}
