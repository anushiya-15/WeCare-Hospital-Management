package com.wecare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "coaches")
public class Coach {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String code;                       // C1001, C1002 ... (generated)

    @NotBlank(message = "Coach name is required") @Size(min = 3, max = 60, message = "Name must be 3-60 characters")
    private String name;

    @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required") @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female or Other")
    private String gender;

    @NotBlank(message = "Mobile number is required") @Pattern(regexp = "[6-9]\\d{9}", message = "Mobile must be 10 digits starting with 6-9")
    @Column(nullable = false, unique = true, length = 10)
    private String mobile;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    public Coach() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
