package com.nhom4.moviereservation.model;

import com.nhom4.moviereservation.model.enums.RoleType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRole {
    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;
    
    @Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    // Getters and setters
}