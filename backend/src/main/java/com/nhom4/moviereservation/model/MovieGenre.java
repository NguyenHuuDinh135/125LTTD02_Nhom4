package com.nhom4.moviereservation.model;
import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
class MovieGenreId implements Serializable {
    private Long movieId;
    private Long genreId;
}
@Entity
@Table(name = "movie_genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenre {

    @EmbeddedId
    private MovieGenreId id;

    @ManyToOne
    @MapsId("movieId")  // ánh xạ tới field movieId trong MovieGenreId
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @MapsId("genreId") // ánh xạ tới field genreId trong MovieGenreId
    @JoinColumn(name = "genre_id")
    private Genre genre;
}