package com.nhom4.moviereservation.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Table(name = "movie_genres", indexes = @Index(name = "fk_mgenres_genre_id", columnList = "genre_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenre {
    @EmbeddedId
    private MovieGenreId id;

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id", foreignKey = @ForeignKey(name = "fk_mgenres_movie_id"))
    private Movie movie;

    @ManyToOne
    @MapsId("genreId")
    @JoinColumn(name = "genre_id", foreignKey = @ForeignKey(name = "fk_mgenres_genre_id"))
    private Genre genre;
}