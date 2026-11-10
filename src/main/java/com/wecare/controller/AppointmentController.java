package com.wecare.controller;

import com.wecare.dto.*;
import com.wecare.entity.Appointment;
import com.wecare.entity.Slot;
import com.wecare.service.AppointmentService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) { this.service = service; }

    @PostMapping("/appointments") @ResponseStatus(HttpStatus.CREATED)
    public Appointment book(@Valid @RequestBody AppointmentRequest r) { return service.book(r); }

    @GetMapping("/appointments")
    public List<Appointment> list(@RequestParam(required = false) String status, @RequestParam(required = false) String coachCode,
                                  @RequestParam(required = false) String userCode,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.list(status, coachCode, userCode, date);
    }

    @GetMapping("/appointments/{code}") public Appointment get(@PathVariable String code) { return service.get(code); }

    @PutMapping("/appointments/{code}/reschedule")
    public Appointment reschedule(@PathVariable String code, @Valid @RequestBody RescheduleRequest r) { return service.reschedule(code, r); }

    @PutMapping("/appointments/{code}/cancel") public Appointment cancel(@PathVariable String code) { return service.cancel(code); }

    @PutMapping("/appointments/{code}/complete")
    public Appointment complete(@PathVariable String code, @RequestBody(required = false) Map<String, String> body) {
        return service.complete(code, body == null ? null : body.get("notes"));
    }

    @GetMapping("/slots")
    public List<Map<String, String>> slotList() {
        List<Map<String, String>> l = new ArrayList<>();
        for (Slot s : Slot.values()) l.add(Map.of("slot", s.name(), "label", s.label()));
        return l;
    }

    @GetMapping("/stats") public Map<String, Object> stats() { return service.stats(); }
}
