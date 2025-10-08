package com.nhom4.moviereservation.repository;

import com.nhom4.moviereservation.model.Movie;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    
    @Query("SELECT m FROM Movie m JOIN m.movieGenres mg WHERE mg.genre.id = :genreId")
    List<Movie> findByGenreId(@Param("genreId") Integer genreId);
}