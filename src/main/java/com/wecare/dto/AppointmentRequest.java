package com.wecare.dto;

import com.wecare.entity.Slot;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AppointmentRequest(
    @NotBlank(message = "Select a member") String userCode,
    @NotBlank(message = "Select a coach") String coachCode,
    @NotNull(message = "Select an appointment date") LocalDate appointmentDate,
    @NotNull(message = "Select a time slot") Slot slot,
    @Size(max = 300, message = "Reason is too long (max 300 characters)") String reason) {}
