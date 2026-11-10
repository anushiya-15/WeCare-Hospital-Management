package com.wecare.dto;

import com.wecare.entity.Slot;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RescheduleRequest(
    @NotNull(message = "Select a new date") LocalDate appointmentDate,
    @NotNull(message = "Select a new time slot") Slot slot) {}
