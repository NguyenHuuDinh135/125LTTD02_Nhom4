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
@Table(name = "genres")
@Data
@NoArgsConstructor  
@AllArgsConstructor
public class Genre{

    @Id
    private Long id;

    private String genre;

    @OneToMany(mappedBy = "genre")
    private List<MovieGenre> movieGenres;

    // Getters and setters
}