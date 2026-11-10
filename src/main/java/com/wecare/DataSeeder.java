package com.wecare;

import com.wecare.dto.AppointmentRequest;
import com.wecare.entity.*;
import com.wecare.repository.CoachRepository;
import com.wecare.service.AppointmentService;
import com.wecare.service.PeopleService;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** First-run sample data: 4 coaches, 5 users and a few appointments. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final PeopleService people;
    private final AppointmentService appts;
    private final CoachRepository coachRepo;
    public DataSeeder(PeopleService people, AppointmentService appts, CoachRepository coachRepo) { this.people = people; this.appts = appts; this.coachRepo = coachRepo; }

    private Coach coach(String n, String dob, String g, String m, String spec) {
        Coach c = new Coach(); c.setName(n); c.setDateOfBirth(LocalDate.parse(dob)); c.setGender(g); c.setMobile(m); c.setSpecialization(spec);
        return people.createCoach(c);
    }
    private AppUser user(String n, String dob, String g, String m, String e, String pin, String city, String state) {
        AppUser u = new AppUser(); u.setName(n); u.setDateOfBirth(LocalDate.parse(dob)); u.setGender(g); u.setMobile(m); u.setEmail(e);
        u.setPincode(pin); u.setCity(city); u.setState(state); u.setCountry("India");
        return people.createUser(u);
    }

    @Override
    public void run(String... args) {
        if (coachRepo.count() > 0) return;
        Coach c1 = coach("Dr. Meera Nair", "1985-03-12", "Female", "9811100001", "Nutrition & Diet");
        Coach c2 = coach("Arjun Menon", "1988-07-25", "Male", "9811100002", "Fitness & Yoga");
        coach("Dr. Sana Iqbal", "1982-11-02", "Female", "9811100003", "Mental Wellness");
        coach("Rohit Verma", "1990-01-19", "Male", "9811100004", "Weight Management");
        AppUser u1 = user("Karthik Raja", "1992-05-14", "Male", "9822200001", "karthik@mail.com", "600001", "Chennai", "Tamil Nadu");
        AppUser u2 = user("Priya Sharma", "1989-09-30", "Female", "9822200002", "priya@mail.com", "560001", "Bengaluru", "Karnataka");
        user("Anand Kumar", "1978-12-08", "Male", "9822200003", "anand@mail.com", "641001", "Coimbatore", "Tamil Nadu");
        user("Lakshmi Devi", "1965-04-21", "Female", "9822200004", "lakshmi@mail.com", "625001", "Madurai", "Tamil Nadu");
        user("Imran Khan", "1995-02-17", "Male", "9822200005", "imran@mail.com", "500001", "Hyderabad", "Telangana");
        LocalDate t = LocalDate.now().plusDays(1);
        appts.book(new AppointmentRequest(u1.getCode(), c1.getCode(), t, Slot.S10, "Diet plan for weight loss"));
        appts.book(new AppointmentRequest(u2.getCode(), c1.getCode(), t, Slot.S11, "Thyroid friendly diet"));
        appts.book(new AppointmentRequest(u1.getCode(), c2.getCode(), t.plusDays(1), Slot.S15, "Beginner yoga routine"));
    }
}
