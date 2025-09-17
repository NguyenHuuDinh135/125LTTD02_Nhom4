package com.nhom4.moviereservation.model;

import java.io.Serializable;

import com.nhom4.moviereservation.model.enums.RoleType;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class MovieRoleId implements Serializable {
    private Long movieId;
    private Long roleId;
}

@Entity
@Table(name = "movie_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRole {
    @EmbeddedId
    private MovieRoleId id;

    @ManyToOne
    @MapsId("movieId")  // ánh xạ movieId trong MovieRoleId
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @MapsId("roleId")  // ánh xạ roleId trong MovieRoleId
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    // Getters and setters
}