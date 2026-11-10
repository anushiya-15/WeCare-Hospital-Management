package com.wecare.repository;

import com.wecare.entity.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByCode(String code);
    List<Appointment> findAllByOrderByAppointmentDateDescSlotDesc();
    List<Appointment> findByCoachIdAndAppointmentDateAndStatusIn(Long coachId, LocalDate date, Collection<AppointmentStatus> statuses);
    List<Appointment> findByUserIdAndAppointmentDateAndStatusIn(Long userId, LocalDate date, Collection<AppointmentStatus> statuses);
    List<Appointment> findByAppointmentDateAndStatusOrderBySlot(LocalDate date, AppointmentStatus status);
    boolean existsByCoachId(Long coachId);
    boolean existsByUserId(Long userId);
    long countByStatus(AppointmentStatus status);

    @Query("select a.coach.name, count(a) from Appointment a where a.status <> com.wecare.entity.AppointmentStatus.CANCELLED group by a.coach.name order by count(a) desc")
    List<Object[]> workloadByCoach();
}
