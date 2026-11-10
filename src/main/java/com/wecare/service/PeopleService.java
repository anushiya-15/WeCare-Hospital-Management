package com.wecare.service;

import com.wecare.entity.AppUser;
import com.wecare.entity.Coach;
import com.wecare.exception.BadRequestException;
import com.wecare.exception.NotFoundException;
import com.wecare.repository.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD + business validation for Coaches and Users (members). */
@Service
public class PeopleService {
    private final CoachRepository coaches;
    private final UserRepository users;
    private final AppointmentRepository appts;

    public PeopleService(CoachRepository coaches, UserRepository users, AppointmentRepository appts) {
        this.coaches = coaches; this.users = users; this.appts = appts;
    }

    private static void checkAge(LocalDate dob, int min, int max, String who) {
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < min || age > max) throw new BadRequestException(who + " age must be between " + min + " and " + max + " years (entered: " + age + ")");
    }

    // ---------- coaches ----------
    public List<Coach> allCoaches() { return coaches.findAllByOrderByNameAsc(); }
    public Coach coach(String code) { return coaches.findByCode(code).orElseThrow(() -> new NotFoundException("Coach not found: " + code)); }

    @Transactional
    public Coach createCoach(Coach c) {
        checkAge(c.getDateOfBirth(), 22, 70, "Coach");
        if (coaches.existsByMobile(c.getMobile())) throw new BadRequestException("Mobile " + c.getMobile() + " is already registered to a coach");
        c.setId(null); c.setName(c.getName().trim());
        c.setCode("TMP" + System.nanoTime() % 1000000000L);
        c = coaches.save(c);
        c.setCode("C" + (1000 + c.getId()));
        return coaches.save(c);
    }

    @Transactional
    public Coach updateCoach(String code, Coach in) {
        Coach c = coach(code);
        checkAge(in.getDateOfBirth(), 22, 70, "Coach");
        if (coaches.existsByMobileAndIdNot(in.getMobile(), c.getId())) throw new BadRequestException("Mobile " + in.getMobile() + " is already registered to another coach");
        c.setName(in.getName().trim()); c.setDateOfBirth(in.getDateOfBirth()); c.setGender(in.getGender());
        c.setMobile(in.getMobile()); c.setSpecialization(in.getSpecialization());
        return coaches.save(c);
    }

    @Transactional
    public void deleteCoach(String code) {
        Coach c = coach(code);
        if (appts.existsByCoachId(c.getId())) throw new BadRequestException("Coach " + code + " has appointments and cannot be deleted");
        coaches.delete(c);
    }

    // ---------- users ----------
    public List<AppUser> allUsers() { return users.findAllByOrderByNameAsc(); }
    public AppUser user(String code) { return users.findByCode(code).orElseThrow(() -> new NotFoundException("User not found: " + code)); }

    @Transactional
    public AppUser createUser(AppUser u) {
        checkAge(u.getDateOfBirth(), 20, 100, "User");
        if (users.existsByMobile(u.getMobile())) throw new BadRequestException("Mobile " + u.getMobile() + " is already registered");
        if (users.existsByEmailIgnoreCase(u.getEmail())) throw new BadRequestException("Email " + u.getEmail() + " is already registered");
        u.setId(null); u.setName(u.getName().trim()); u.setEmail(u.getEmail().trim().toLowerCase());
        u.setCode("TMP" + System.nanoTime() % 1000000000L);
        u = users.save(u);
        u.setCode("U" + (1000 + u.getId()));
        return users.save(u);
    }

    @Transactional
    public AppUser updateUser(String code, AppUser in) {
        AppUser u = user(code);
        checkAge(in.getDateOfBirth(), 20, 100, "User");
        if (users.existsByMobileAndIdNot(in.getMobile(), u.getId())) throw new BadRequestException("Mobile " + in.getMobile() + " is already registered to another user");
        if (users.existsByEmailIgnoreCaseAndIdNot(in.getEmail(), u.getId())) throw new BadRequestException("Email " + in.getEmail() + " is already registered to another user");
        u.setName(in.getName().trim()); u.setDateOfBirth(in.getDateOfBirth()); u.setGender(in.getGender()); u.setMobile(in.getMobile());
        u.setEmail(in.getEmail().trim().toLowerCase()); u.setPincode(in.getPincode()); u.setCity(in.getCity()); u.setState(in.getState()); u.setCountry(in.getCountry());
        return users.save(u);
    }

    @Transactional
    public void deleteUser(String code) {
        AppUser u = user(code);
        if (appts.existsByUserId(u.getId())) throw new BadRequestException("User " + code + " has appointments and cannot be deleted");
        users.delete(u);
    }
}
