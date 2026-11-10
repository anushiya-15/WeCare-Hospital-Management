package com.wecare.service;

import com.wecare.dto.*;
import com.wecare.entity.*;
import com.wecare.exception.BadRequestException;
import com.wecare.exception.NotFoundException;
import com.wecare.repository.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    public static final int WINDOW_DAYS = 7;
    public static final int MIN_NOTICE_HOURS = 2;
    private static final List<AppointmentStatus> ACTIVE = List.of(AppointmentStatus.BOOKED, AppointmentStatus.COMPLETED);

    private final AppointmentRepository repo;
    private final PeopleService people;
    private final CoachRepository coachRepo;
    private final UserRepository userRepo;

    public AppointmentService(AppointmentRepository repo, PeopleService people, CoachRepository coachRepo, UserRepository userRepo) {
        this.repo = repo; this.people = people; this.coachRepo = coachRepo; this.userRepo = userRepo;
    }

    private static LocalDateTime startOf(Appointment a) { return a.getAppointmentDate().atTime(a.getSlot().start()); }

    private void validateWindow(LocalDate date, Slot slot) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) throw new BadRequestException("Appointment date cannot be in the past");
        if (date.isAfter(today.plusDays(WINDOW_DAYS))) throw new BadRequestException("Appointments can be booked only up to " + WINDOW_DAYS + " days in advance");
        if (date.equals(today) && !slot.start().isAfter(LocalTime.now())) throw new BadRequestException("The " + slot.label() + " slot has already started today");
    }

    private void checkConflicts(Coach coach, AppUser user, LocalDate date, Slot slot, Long ignoreId) {
        boolean coachBusy = repo.findByCoachIdAndAppointmentDateAndStatusIn(coach.getId(), date, ACTIVE).stream()
                .anyMatch(a -> a.getSlot() == slot && !a.getId().equals(ignoreId));
        if (coachBusy) throw new BadRequestException("Coach " + coach.getName() + " is not available at " + slot.label() + " on " + date);
        boolean userBusy = repo.findByUserIdAndAppointmentDateAndStatusIn(user.getId(), date, ACTIVE).stream()
                .anyMatch(a -> a.getSlot() == slot && !a.getId().equals(ignoreId));
        if (userBusy) throw new BadRequestException(user.getName() + " already has another appointment at " + slot.label() + " on " + date);
    }

    @Transactional
    public Appointment book(AppointmentRequest r) {
        AppUser user = people.user(r.userCode());
        Coach coach = people.coach(r.coachCode());
        validateWindow(r.appointmentDate(), r.slot());
        checkConflicts(coach, user, r.appointmentDate(), r.slot(), -1L);
        Appointment a = new Appointment();
        a.setCode("TMP" + System.nanoTime() % 1000000000L);
        a.setUser(user); a.setCoach(coach); a.setAppointmentDate(r.appointmentDate()); a.setSlot(r.slot());
        a.setReason(r.reason() == null ? null : r.reason().trim());
        a.setStatus(AppointmentStatus.BOOKED); a.setRescheduleCount(0);
        a.setCreatedAt(LocalDateTime.now()); a.setUpdatedAt(LocalDateTime.now());
        a = repo.save(a);
        a.setCode("AP" + (100000 + a.getId()));
        return repo.save(a);
    }

    @Transactional
    public Appointment reschedule(String code, RescheduleRequest r) {
        Appointment a = get(code);
        if (a.getStatus() != AppointmentStatus.BOOKED) throw new BadRequestException("Only BOOKED appointments can be rescheduled (current: " + a.getStatus() + ")");
        if (Duration.between(LocalDateTime.now(), startOf(a)).toMinutes() < MIN_NOTICE_HOURS * 60L)
            throw new BadRequestException("Appointments cannot be rescheduled within " + MIN_NOTICE_HOURS + " hours of the start time");
        if (a.getAppointmentDate().equals(r.appointmentDate()) && a.getSlot() == r.slot()) throw new BadRequestException("The new date and slot are the same as the current ones");
        validateWindow(r.appointmentDate(), r.slot());
        checkConflicts(a.getCoach(), a.getUser(), r.appointmentDate(), r.slot(), a.getId());
        a.setAppointmentDate(r.appointmentDate()); a.setSlot(r.slot());
        a.setRescheduleCount(a.getRescheduleCount() + 1); a.setUpdatedAt(LocalDateTime.now());
        return repo.save(a);
    }

    @Transactional
    public Appointment cancel(String code) {
        Appointment a = get(code);
        if (a.getStatus() != AppointmentStatus.BOOKED) throw new BadRequestException("Only BOOKED appointments can be cancelled (current: " + a.getStatus() + ")");
        if (!startOf(a).isAfter(LocalDateTime.now())) throw new BadRequestException("This appointment has already started and cannot be cancelled");
        a.setStatus(AppointmentStatus.CANCELLED); a.setUpdatedAt(LocalDateTime.now());
        return repo.save(a);
    }

    /** The coach records the session as completed (allowed on the appointment day or later). */
    @Transactional
    public Appointment complete(String code, String notes) {
        Appointment a = get(code);
        if (a.getStatus() != AppointmentStatus.BOOKED) throw new BadRequestException("Only BOOKED appointments can be completed (current: " + a.getStatus() + ")");
        if (a.getAppointmentDate().isAfter(LocalDate.now())) throw new BadRequestException("An appointment can be completed only on or after its appointment date");
        if (notes != null && notes.length() > 600) throw new BadRequestException("Notes are too long (max 600 characters)");
        a.setStatus(AppointmentStatus.COMPLETED); a.setCoachNotes(notes == null ? null : notes.trim()); a.setUpdatedAt(LocalDateTime.now());
        return repo.save(a);
    }

    public Appointment get(String code) { return repo.findByCode(code.trim().toUpperCase()).orElseThrow(() -> new NotFoundException("Appointment not found: " + code)); }

    public List<Appointment> list(String status, String coachCode, String userCode, LocalDate date) {
        AppointmentStatus st = null;
        if (status != null && !status.isBlank()) {
            try { st = AppointmentStatus.valueOf(status.toUpperCase()); } catch (Exception e) { throw new BadRequestException("Invalid status: " + status); }
        }
        final AppointmentStatus fs = st;
        return repo.findAllByOrderByAppointmentDateDescSlotDesc().stream()
                .filter(a -> fs == null || a.getStatus() == fs)
                .filter(a -> coachCode == null || coachCode.isBlank() || a.getCoach().getCode().equalsIgnoreCase(coachCode))
                .filter(a -> userCode == null || userCode.isBlank() || a.getUser().getCode().equalsIgnoreCase(userCode))
                .filter(a -> date == null || a.getAppointmentDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<SlotInfo> slots(String coachCode, LocalDate date) {
        Coach coach = people.coach(coachCode);
        if (date.isAfter(LocalDate.now().plusDays(WINDOW_DAYS))) throw new BadRequestException("Appointments can be booked only up to " + WINDOW_DAYS + " days in advance");
        Set<Slot> taken = repo.findByCoachIdAndAppointmentDateAndStatusIn(coach.getId(), date, ACTIVE).stream().map(Appointment::getSlot).collect(Collectors.toSet());
        LocalDate today = LocalDate.now();
        List<SlotInfo> out = new ArrayList<>();
        for (Slot s : Slot.values()) {
            boolean past = date.isBefore(today) || (date.equals(today) && !s.start().isAfter(LocalTime.now()));
            out.add(new SlotInfo(s.name(), s.label(), past ? "PAST" : taken.contains(s) ? "TAKEN" : "AVAILABLE"));
        }
        return out;
    }

    public Map<String, Object> stats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("coaches", coachRepo.count());
        m.put("users", userRepo.count());
        m.put("booked", repo.countByStatus(AppointmentStatus.BOOKED));
        m.put("completed", repo.countByStatus(AppointmentStatus.COMPLETED));
        m.put("cancelled", repo.countByStatus(AppointmentStatus.CANCELLED));
        m.put("today", repo.findByAppointmentDateAndStatusOrderBySlot(LocalDate.now(), AppointmentStatus.BOOKED).size());
        List<Map<String, Object>> load = new ArrayList<>();
        for (Object[] row : repo.workloadByCoach()) load.add(Map.of("coach", row[0], "appointments", row[1]));
        m.put("workload", load);
        return m;
    }
}
