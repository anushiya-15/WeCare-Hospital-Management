package com.wecare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String code;                       // AP100001 ...

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "coach_id")
    private Coach coach;

    private LocalDate appointmentDate;

    @Enumerated(EnumType.STRING) @Column(length = 5)
    private Slot slot;

    @Column(length = 300)
    private String reason;

    @Column(length = 600)
    private String coachNotes;

    @Enumerated(EnumType.STRING) @Column(length = 12)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    private Integer rescheduleCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Appointment() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public Slot getSlot() { return slot; }
    public void setSlot(Slot slot) { this.slot = slot; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getCoachNotes() { return coachNotes; }
    public void setCoachNotes(String coachNotes) { this.coachNotes = coachNotes; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public Integer getRescheduleCount() { return rescheduleCount; }
    public void setRescheduleCount(Integer rescheduleCount) { this.rescheduleCount = rescheduleCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
