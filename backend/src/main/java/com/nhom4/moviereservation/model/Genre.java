package com.nhom4.moviereservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre extends BaseEntity {

    private String name;

    @OneToMany(mappedBy = "genre")
    private List<MovieGenre> movieGenres;

    // Getters and setters
}