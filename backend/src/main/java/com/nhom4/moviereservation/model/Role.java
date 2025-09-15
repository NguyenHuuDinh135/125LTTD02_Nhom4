package com.nhom4.moviereservation.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    private Long id;
    
    private String fullName;
    private Byte age;
    private String pictureUrl;

    @OneToMany(mappedBy = "role")
    private List<MovieRole> movieRoles;

    // Getters and setters
}