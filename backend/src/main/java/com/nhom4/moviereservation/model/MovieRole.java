package com.nhom4.moviereservation.model;


import com.nhom4.moviereservation.model.enums.RoleType;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "movie_roles",
       indexes = @Index(name = "fk_mroles_role_id", columnList = "role_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRole {
    @EmbeddedId
    private MovieRoleId id;

    @ManyToOne
    @MapsId("movieId")  // ánh xạ movieId trong MovieRoleId
    @JoinColumn(name = "movie_id", foreignKey = @ForeignKey(name = "fk_mroles_movie_id"))
    private Movie movie;

    @ManyToOne
    @MapsId("roleId")  // ánh xạ roleId trong MovieRoleId
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_mroles_role_id"))
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType;

    // Getters and setters
}