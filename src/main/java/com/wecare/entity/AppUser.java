package com.wecare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "wecare_users")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String code;                       // U1001, U1002 ... (generated)

    @NotBlank(message = "Name is required") @Size(min = 3, max = 60, message = "Name must be 3-60 characters")
    private String name;

    @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required") @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female or Other")
    private String gender;

    @NotBlank(message = "Mobile number is required") @Pattern(regexp = "[6-9]\\d{9}", message = "Mobile must be 10 digits starting with 6-9")
    @Column(nullable = false, unique = true, length = 10)
    private String mobile;

    @NotBlank(message = "Email is required") @Email(message = "Enter a valid email")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Pincode is required") @Pattern(regexp = "\\d{6}", message = "Pincode must be exactly 6 digits")
    @Column(length = 6)
    private String pincode;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    public AppUser() {}
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
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
