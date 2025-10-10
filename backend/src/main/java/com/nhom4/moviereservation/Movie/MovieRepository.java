package com.nhom4.moviereservation.Movie;

import com.nhom4.moviereservation.model.Movie;
import com.nhom4.moviereservation.model.enums.MovieType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
   List<Movie> findByMovieType(MovieType movieType);
}