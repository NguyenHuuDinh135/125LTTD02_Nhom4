package com.nhom4.moviereservation.model;

import java.time.LocalDateTime;
import java.util.List;


import jakarta.persistence.Column;

import com.nhom4.moviereservation.model.enums.UserRole;

import com.nhom4.moviereservation.model.enums.Role;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.nhom4.moviereservation.model.enums.UserRole;

@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(name = "email", columnNames = "email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    private Long id;

    
    @Column(name = "full_name", length = 50)
    private String fullName;
    @Column(length = 50)
    private String email;
    private String password;
    @Column(length = 100)
    private String address;
    private String contact;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

     @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "user")
    private List<Payment> payments;

    // Getters and setters
}