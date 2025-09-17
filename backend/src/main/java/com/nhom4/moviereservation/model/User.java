package com.nhom4.moviereservation.model;

import java.util.List;

import com.nhom4.moviereservation.model.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private Long id;
    private String fullname;
    private String email;
    private String password;
    private String address;
    private String contact;
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "user")
    private List<Payment> payments;

    // Getters and setters
}