package com.nhom4.moviereservation.model;

import java.math.BigDecimal;
import java.util.List;

import com.nhom4.moviereservation.model.enums.MovieType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    private Long id;
    private String title;
    private String summary;
    private Integer year;
    private BigDecimal rating;
    private String trailerUrl;
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    private MovieType movieType;

    @OneToMany(mappedBy = "movie")
    private List<Show> shows;

    @OneToMany(mappedBy = "movie")
    private List<MovieGenre> movieGenres;

    @OneToMany(mappedBy = "movie")
    private List<MovieRole> movieRoles;

    // Getters and setters
}