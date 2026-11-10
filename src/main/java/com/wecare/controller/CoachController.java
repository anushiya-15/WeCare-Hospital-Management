package com.wecare.controller;

import com.wecare.dto.SlotInfo;
import com.wecare.entity.Coach;
import com.wecare.service.AppointmentService;
import com.wecare.service.PeopleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coaches")
public class CoachController {
    private final PeopleService people;
    private final AppointmentService appointments;
    public CoachController(PeopleService people, AppointmentService appointments) { this.people = people; this.appointments = appointments; }

    @GetMapping public List<Coach> all() { return people.allCoaches(); }
    @GetMapping("/{code}") public Coach get(@PathVariable String code) { return people.coach(code); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Coach create(@Valid @RequestBody Coach c) { return people.createCoach(c); }

    @PutMapping("/{code}") public Coach update(@PathVariable String code, @Valid @RequestBody Coach c) { return people.updateCoach(code, c); }

    @DeleteMapping("/{code}") public Map<String, String> delete(@PathVariable String code) { people.deleteCoach(code); return Map.of("message", "Deleted " + code); }

    /** GET /api/coaches/C1001/slots?date=2026-10-15  -> availability of every slot */
    @GetMapping("/{code}/slots")
    public List<SlotInfo> slots(@PathVariable String code, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointments.slots(code, date);
    }
}
