package com.wecare.controller;

import com.wecare.entity.AppUser;
import com.wecare.service.PeopleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final PeopleService people;
    public UserController(PeopleService people) { this.people = people; }

    @GetMapping public List<AppUser> all() { return people.allUsers(); }
    @GetMapping("/{code}") public AppUser get(@PathVariable String code) { return people.user(code); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AppUser create(@Valid @RequestBody AppUser u) { return people.createUser(u); }

    @PutMapping("/{code}") public AppUser update(@PathVariable String code, @Valid @RequestBody AppUser u) { return people.updateUser(code, u); }

    @DeleteMapping("/{code}") public Map<String, String> delete(@PathVariable String code) { people.deleteUser(code); return Map.of("message", "Deleted " + code); }
}
